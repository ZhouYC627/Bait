package controllers.limitdepthfirst;

import java.awt.*;
import java.util.ArrayList;


import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

/**
 * Created by zyc on 3/12/17.
 */
public class Agent extends AbstractPlayer{
    /**
     * Observation grid.
     */
    protected ArrayList<Observation> grid[][];

    /**
     * block size
     */
    protected int block_size;
    private boolean foundPath;
    protected ArrayList<Types.ACTIONS> resultActions;
    protected ArrayList<Types.ACTIONS> tempActions;
    protected ArrayList<StateObservation> stateArrary;
    private double heuValue;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        //randomGenerator = new Random();
        foundPath = false;
        heuValue = 999999;
        tempActions = new ArrayList<>();
        resultActions = new ArrayList<>();
        stateArrary = new ArrayList<>();
        //resultActions.clear();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
        /*int depth = 1;
        while (!foundPath){
            DFS(so, depth);
            depth += 2;
        }*/
    }

    private double heuristic(StateObservation stateObs){
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        Vector2d goalpos = fixedPositions[1].get(0).position;
        Vector2d avatarpos = stateObs.getAvatarPosition();

        double res = 0;
        if (!movingPositions[0].isEmpty()){
            Vector2d keypos = movingPositions[0].get(0).position;
            res += avatarpos.dist(keypos) + keypos.dist(goalpos);
        }else{
            res += avatarpos.dist(goalpos);
        }
        return res;
    }
    private void DFS(StateObservation so, int depth){

        stateArrary.add(so);
        ArrayList<Types.ACTIONS> actions = so.getAvailableActions();
        if (heuristic(so)<heuValue){
            heuValue = heuristic(so);
            System.out.println(heuValue);
            resultActions.clear();
            resultActions = (ArrayList<Types.ACTIONS>) tempActions.clone();
        }
        for (Types.ACTIONS actionTry: actions) {
            if (foundPath) break;
            StateObservation stCopy = so.copy();
            stCopy.advance(actionTry);
            tempActions.add(actionTry);
            if (stCopy.isGameOver()){
                System.out.println("Step: " + stateArrary.size());
                foundPath = true;
                resultActions.clear();
                resultActions = (ArrayList<Types.ACTIONS>) tempActions.clone();
            }else{
                //判断是否形成回路,如果没有就继续搜索
                boolean isLoop = false;
                for (StateObservation s: stateArrary){
                    if (s.equalPosition(stCopy)){
                        isLoop = true;
                        break;
                    }
                }
                if (!isLoop && depth > 1){
                    DFS(stCopy, depth -1);
                }
            }
            tempActions.remove(actionTry);
        }
        stateArrary.remove(so);
    }
    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        int depth = 10;
        heuValue = 999999;
        DFS(stateObs, depth);
        //return resultActions.remove(resultActions.size()-1);
        System.out.println(resultActions.size());
        return resultActions.remove(0);
    }

    /**
     * Gets the player the control to draw something on the screen.
     * It can be used for debug purposes.
     * @param g Graphics device to draw to.
     */
    public void draw(Graphics2D g)
    {
        int half_block = (int) (block_size*0.5);
        for(int j = 0; j < grid[0].length; ++j)
        {
            for(int i = 0; i < grid.length; ++i)
            {
                if(grid[i][j].size() > 0)
                {
                    Observation firstObs = grid[i][j].get(0); //grid[i][j].size()-1
                    //Three interesting options:
                    int print = firstObs.category; //firstObs.itype; //firstObs.obsID;
                    g.drawString(print + "", i*block_size+half_block,j*block_size+half_block);
                }
            }
        }
    }
}
