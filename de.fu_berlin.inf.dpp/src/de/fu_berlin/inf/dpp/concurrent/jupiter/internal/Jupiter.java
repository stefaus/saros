/*
 * $Id: Jupiter.java 2859 2006-04-01 09:39:19Z sim $
 *
 * ace - a collaborative editor
 * Copyright (C) 2005 Mark Bigler, Simon Raess, Lukas Zbinden
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package de.fu_berlin.inf.dpp.concurrent.jupiter.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm;
import de.fu_berlin.inf.dpp.concurrent.jupiter.InclusionTransformation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Operation;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp;
import de.fu_berlin.inf.dpp.concurrent.jupiter.TransformationException;
import de.fu_berlin.inf.dpp.concurrent.jupiter.internal.text.GOTOInclusionTransformation;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * This class implements the client-side core of the Jupiter control algorithm.
 */
public class Jupiter implements Algorithm {

    /**
     * The inclusion transformation function used to transform operations.
     */
    protected InclusionTransformation inclusion;

    /**
     * The vector time, representing the number of processed JupiterActivities,
     * of this algorithm.
     */
    protected JupiterVectorTime vectorTime;

    /**
     * Flag indicating whether this algorithm is used on the client-side. In
     * some situations, the JupiterActivities from the server-side have a higher
     * priority in transformations.
     */
    protected final boolean isClientSide;

    /**
     * A list that contains the JupiterActivities sent to the server which are
     * to be acknowledged by the server before they can be removed. This list
     * corresponds to the 'outgoing' list in the Jupiter pseudo code
     * description.
     */
    protected final List<OperationWrapper> ackJupiterActivityList;

    /**
     * Class constructor that creates a new Jupiter algorithm.
     * 
     * @param isClientSide
     *            true if the algorithm resides on the client side
     */
    public Jupiter(boolean isClientSide) {
        this.inclusion = new GOTOInclusionTransformation();
        this.vectorTime = new JupiterVectorTime(0, 0);
        this.isClientSide = isClientSide;
        this.ackJupiterActivityList = new ArrayList<OperationWrapper>();
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#generateJupiterActivity(de.fu_berlin.inf.dpp.concurrent.jupiter.Operation,
     *      de.fu_berlin.inf.dpp.net.JID, IPath)
     */
    public JupiterActivity generateJupiterActivity(Operation op, JID jid,
        IPath editor) {

        // send(op, myMsgs, otherMsgs);
        JupiterActivity jupiterActivity = new JupiterActivity(this.vectorTime,
            op, jid, editor);

        // add(op, myMsgs) to outgoing;
        this.ackJupiterActivityList.add(new OperationWrapper(op,
            this.vectorTime.getLocalOperationCount()));

        // myMsgs = myMsgs + 1;
        this.vectorTime = this.vectorTime.incrementLocalOperationCount();

        return jupiterActivity;
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#receiveJupiterActivity(de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity)
     */
    public Operation receiveJupiterActivity(JupiterActivity jupiterActivity)
        throws TransformationException {
        Timestamp timestamp = jupiterActivity.getTimestamp();
        if (!(timestamp instanceof JupiterVectorTime)) {
            throw new IllegalArgumentException(
                "Jupiter expects timestamps of type JupiterVectorTime");
        }
        checkPreconditions((JupiterVectorTime) timestamp);
        discardAcknowledgedOperations((JupiterVectorTime) timestamp);

        Operation newOp = transform(jupiterActivity.getOperation());
        this.vectorTime = this.vectorTime.incrementRemoteOperationCount();

        return newOp;
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#acknowledge(int,
     *      de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp)
     */
    public void acknowledge(int siteId, Timestamp timestamp)
        throws TransformationException {
        discardAcknowledgedOperations((JupiterVectorTime) timestamp);
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#transformIndices(de.fu_berlin.inf.dpp.concurrent.jupiter.Timestamp,
     *      int[])
     */
    public int[] transformIndices(Timestamp timestamp, int[] indices)
        throws TransformationException {
        checkPreconditions((JupiterVectorTime) timestamp);
        discardAcknowledgedOperations((JupiterVectorTime) timestamp);
        int[] result = new int[indices.length];
        System.arraycopy(indices, 0, result, 0, indices.length);
        for (int i = 0; i < this.ackJupiterActivityList.size(); i++) {
            OperationWrapper wrap = this.ackJupiterActivityList.get(i);
            Operation ack = wrap.getOperation();
            for (int k = 0; k < indices.length; k++) {
                result[k] = transformIndex(result[k], ack);
            }
        }
        return result;
    }

    /**
     * Transforms the given index against the operation.
     * 
     * @param index
     *            the index to be transformed
     * @param op
     *            the operation to be transformed
     * @return the transformed index
     */
    protected int transformIndex(int index, Operation op) {
        if (isClientSide()) {
            return this.inclusion.transformIndex(index, op, Boolean.TRUE);
        } else {
            return this.inclusion.transformIndex(index, op, Boolean.FALSE);
        }
    }

    /**
     * Discard from the other site (client/server) acknowledged operations.
     * 
     * @param time
     *            the remote JupiterVectorTime
     */
    protected void discardAcknowledgedOperations(JupiterVectorTime time) {
        Iterator<OperationWrapper> iter = this.ackJupiterActivityList
            .iterator();
        while (iter.hasNext()) {
            OperationWrapper wrap = iter.next();
            if (wrap.getLocalOperationCount() < time.getRemoteOperationCount()) {
                iter.remove();
            }
        }
        // ASSERT msg.myMsgs == otherMsgs
        assert time.getLocalOperationCount() == this.vectorTime
            .getRemoteOperationCount() : "msg.myMsgs != otherMsgs !!";
    }

    /**
     * Transforms an operation with the operations in the outgoing queue
     * {@link #ackJupiterActivityList}.
     * 
     * @param newOp
     *            the operation to be transformed
     * @return the transformed operation
     * @see #ackJupiterActivityList
     */
    protected Operation transform(Operation newOp) {
        for (int ackJupiterActivityListCnt = 0; ackJupiterActivityListCnt < this.ackJupiterActivityList
            .size(); ackJupiterActivityListCnt++) {
            OperationWrapper wrap = this.ackJupiterActivityList
                .get(ackJupiterActivityListCnt);
            Operation existingOp = wrap.getOperation();

            Operation transformedOp;

            if (isClientSide()) {
                transformedOp = this.inclusion.transform(newOp, existingOp,
                    Boolean.TRUE);
                existingOp = this.inclusion.transform(existingOp, newOp,
                    Boolean.FALSE);
            } else {
                transformedOp = this.inclusion.transform(newOp, existingOp,
                    Boolean.FALSE);
                existingOp = this.inclusion.transform(existingOp, newOp,
                    Boolean.TRUE);
            }
            this.ackJupiterActivityList
                .set(ackJupiterActivityListCnt, new OperationWrapper(
                    existingOp, wrap.getLocalOperationCount()));

            newOp = transformedOp;
        }
        return newOp;
    }

    /**
     * Test 3 preconditions that must be fulfilled before transforming. They are
     * taken from the Jupiter paper.
     * 
     * @param time
     *            the JupiterActivity to be tested.
     */
    protected void checkPreconditions(JupiterVectorTime time)
        throws TransformationException {
        if (!this.ackJupiterActivityList.isEmpty()
            && (time.getRemoteOperationCount() < this.ackJupiterActivityList
                .get(0).getLocalOperationCount())) {
            throw new TransformationException("Precondition #1 violated.");
        } else if (time.getRemoteOperationCount() > this.vectorTime
            .getLocalOperationCount()) {
            throw new TransformationException(
                "precondition #2 violated (Remote vector time is greater than local vector time).");
        } else if (time.getLocalOperationCount() != this.vectorTime
            .getRemoteOperationCount()) {
            throw new TransformationException(
                "Precondition #3 violated (Vector time does not match): "
                    + time + " , " + this.vectorTime);
        }
    }

    /**
     * This is a simple helper class used in the implementation of the Jupiter
     * algorithm. A OperationWrapper instance is created with an operation and
     * the current local operation count and inserted into the outgoing queue
     * (see {@link Jupiter#ackJupiterActivityList}).
     * 
     * @valueObject Instances of this class should be treated as value objects
     *              and should be treated as immutable.
     * 
     * @see Jupiter#generateJupiterActivity(Operation, JID, IPath)
     * @see Jupiter#receiveJupiterActivity(JupiterActivity)
     */
    protected static class OperationWrapper {

        protected final Operation op;

        protected final int count;

        OperationWrapper(Operation op, int count) {
            this.op = op;
            this.count = count;
        }

        Operation getOperation() {
            return this.op;
        }

        int getLocalOperationCount() {
            return this.count;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return ("OperationWrapper(" + this.op + ", " + this.count + ")");
        }
    }

    /**
     * Throws a CannotUndoException because undo is not supported by this
     * implementation.
     * 
     * This method used to be part of the {@link Algorithm} interface
     */
    public JupiterActivity undo() {
        throw new CannotUndoException();
    }

    /**
     * Throws a CannotRedoException because undo is not supported by this
     * implementation.
     * 
     * This method used to be part of the {@link Algorithm} interface
     */
    public JupiterActivity redo() {
        throw new CannotRedoException();
    }

    /**
     * @see de.fu_berlin.inf.dpp.concurrent.jupiter.Algorithm#getTimestamp()
     */
    public synchronized Timestamp getTimestamp() {
        return this.vectorTime;
    }

    /**
     * Checks if this algorithm locates client side.
     * 
     * @return true if this algorithm locates client side
     */
    public boolean isClientSide() {
        return this.isClientSide;
    }

    public void updateVectorTime(Timestamp timestamp)
        throws TransformationException {
        if (this.ackJupiterActivityList.size() > 0) {
            throw new TransformationException(
                "ackJupiterActivityList have entries. Update Vector time failed.");
        }
        int local = timestamp.getComponents()[0];
        int remote = timestamp.getComponents()[1];
        this.vectorTime = new JupiterVectorTime(local, remote);

    }

}
