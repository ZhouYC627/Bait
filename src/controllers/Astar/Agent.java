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
    private int stepCount;
    private Types.ACTIONS action;
    protected PriorityQueue<AvailableState> availableStates;
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
        stepCount = 0;
        resultAction = new ArrayList<>();
        visitedStates = new ArrayList<>();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
        availableStates = new PriorityQueue<>();


        Astar(so);

        //System.out.println(timeCount + "ms");
    }


    private void Astar(StateObservation so) {
        availableStates.offer(new AvailableState(so));

        //从优先级队列中每次取出一个 f(n) 最小的状态，对其进行搜索
        while (!availableStates.isEmpty() && !foundPath) {
            AvailableState nextState = availableStates.remove();
            System.out.println(nextState.heuValue + ": " + nextState.getSteps() + nextState.stateObs.getAvatarPosition());
            visitedStates.add(nextState.stateObs);
            ArrayList<Types.ACTIONS> actions = nextState.stateObs.getAvailableActions();
            for (Types.ACTIONS actionTry : actions) {
                //if (foundPath) break;
                StateObservation stCopy = nextState.stateObs.copy();
                stCopy.advance(actionTry);

                //long start = System.currentTimeMillis();
                //判断是否形成回路,如果没有就继续搜索
                boolean isLoop = false;
                for (StateObservation s : visitedStates) {
                    if (stCopy.equalPosition(s)) {
                        isLoop = true;
                        break;
                    }
                }
                //long end = System.currentTimeMillis();
                //timeCount = timeCount + end-start;

                if (!isLoop && !stCopy.isGameOver()) {
                    availableStates.offer(new AvailableState(stCopy, nextState));
                } else {
                    if (stCopy.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                        foundPath = true;
                        System.out.println("Bingo!!!");
                        //回溯确定路径
                        resultAction.add(stCopy.getAvatarLastAction());
                        AvailableState it = nextState;
                        while (it.lastState != null){
                            resultAction.add(it.stateObs.getAvatarLastAction());
                            it = it.lastState;
                        }
                    }
                    if (stCopy.isGameOver()){
                        System.out.println(nextState.getSteps() + "; "+ stCopy.getAvatarPosition());
                        System.out.println(stCopy.getGameWinner().toString());
                    }
                    }
            }
            //tempActions.add(actionTry);
            //AvailableState curOptimalSt = availableStates.remove();
            //Astar(curOptimalSt.stateObs, depth + 1);
        }
        /*
        if (foundPath) {
            resultAction.add(so.getAvatarLastAction());
        }
        */
        //System.out.println(so.getAvatarType() + "\n" + so.getGameWinner().toString() + so.getAvatarPosition().toString());
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
