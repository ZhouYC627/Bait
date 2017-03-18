package controllers.Astar;

import java.awt.*;
import java.util.ArrayList;
import java.util.PriorityQueue;


import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created by zyc on 3/12/17.
 */
public class Agent extends AbstractPlayer {
    /**
     * Observation grid.
     */
    protected ArrayList<Observation> grid[][];

    /**
     * block size
     */
    protected int block_size;
    private long timeCount;
    private boolean foundPath;
    private Types.ACTIONS action;
    protected ArrayList<Types.ACTIONS> resultAction;
    protected ArrayList<StateObservation> visitedStates;

    /**
     * Public constructor with state observation and time due.
     *
     * @param so           state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer) {
        foundPath = false;
        resultAction = new ArrayList<>();
        visitedStates = new ArrayList<>();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();


        Astar(so);

        //System.out.println(timeCount + "ms");
    }


    private void Astar(StateObservation so) {
        visitedStates.clear();
        PriorityQueue<AvailableState> availableStates = new PriorityQueue<>();
        availableStates.add(new AvailableState(so));

        //从优先级队列中每次取出一个 f(n) 最小的状态，对其进行搜索
        while (!availableStates.isEmpty() && !foundPath) {

            AvailableState nextState = availableStates.poll();
            ArrayList<Types.ACTIONS> actions = nextState.stateObs.getAvailableActions();

            for (Types.ACTIONS actionTry : actions) {
                StateObservation stCopy = nextState.stateObs.copy();
                stCopy.advance(actionTry);

                //判断是否形成回路,如果没有就继续搜索
                long start = System.currentTimeMillis();
                boolean isLoop = false;
                for (StateObservation s : visitedStates) {
                    if (stCopy.equalPosition(s)) {
                        isLoop = true;
                        break;
                    }
                }
                long end = System.currentTimeMillis();
                timeCount += end - start;

                if (!isLoop && !stCopy.isGameOver()) {
                    availableStates.add(new AvailableState(stCopy, nextState));
                    visitedStates.add(stCopy);
                } else {
                    if (stCopy.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                        foundPath = true;
                        //确定路径
                        resultAction.add(stCopy.getAvatarLastAction());
                        AvailableState it = nextState;
                        while (it.lastState != null){
                            resultAction.add(it.stateObs.getAvatarLastAction());
                            it = it.lastState;
                        }
                    }
                }
            }
        }
        //System.out.println(visitedStates.size());
    }

    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     *
     * @param stateObs     Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if (!resultAction.isEmpty()) {
            action = resultAction.remove(resultAction.size()-1);
        }
        //action = resultAction.get(stepCount++);
        //System.out.println(action.toString());
        return action;
    }

    /**
     * Gets the player the control to draw something on the screen.
     * It can be used for debug purposes.
     *
     * @param g Graphics device to draw to.
     */
    public void draw(Graphics2D g) {
        int half_block = (int) (block_size * 0.5);
        for (int j = 0; j < grid[0].length; ++j) {
            for (int i = 0; i < grid.length; ++i) {
                if (grid[i][j].size() > 0) {
                    Observation firstObs = grid[i][j].get(0); //grid[i][j].size()-1
                    //Three interesting options:
                    int print = firstObs.category; //firstObs.itype; //firstObs.obsID;
                    g.drawString(print + "", i * block_size + half_block, j * block_size + half_block);
                }
            }
        }
    }
}
