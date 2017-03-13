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
    private double heuValue;
    private int finalStep;
    private boolean foundPath;
    private static final int LIMITED_DEPTH = 5;
    private static final int MAX_HEU = 999999;
    protected Types.ACTIONS resultAction;
    protected ArrayList<Types.ACTIONS> tempActions;
    protected ArrayList<StateObservation> stateArrary;


    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        //randomGenerator = new Random();
        foundPath = false;
        tempActions = new ArrayList<>();
        //resultActions = new ArrayList<>();
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

    private double gridDist(Vector2d v0, Vector2d v1){
        return Math.abs(v0.x-v1.x)+Math.abs(v0.y-v1.y);
    }

    private double heuristic(StateObservation stateObs){
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        Vector2d goalpos = fixedPositions[1].get(0).position;
        Vector2d avatarpos = stateObs.getAvatarPosition();
        Vector2d keypos;
        double dist = MAX_HEU;
        if (stateObs.getAvatarType()==1){//没拿到钥匙
            keypos = movingPositions[0].get(0).position;
            dist = gridDist(avatarpos, keypos)*10 + gridDist(keypos, goalpos);
        }else if(stateObs.getAvatarType()==4){//拿到了钥匙
            dist = gridDist(avatarpos, goalpos);
        }
        return dist;
    }
    private void DFS(StateObservation so, int depth){

        if (depth == 0){
            //System.out.println(heuristic(so));
            if (heuristic(so)<heuValue) {
                heuValue = heuristic(so);
                resultAction = tempActions.get(0);
            }
            return;
        }
        stateArrary.add(so);
        ArrayList<Types.ACTIONS> actions = so.getAvailableActions();
        for (Types.ACTIONS actionTry: actions) {
            if (foundPath) break;
            StateObservation stCopy = so.copy();
            stCopy.advance(actionTry);
            tempActions.add(actionTry);
            if (stCopy.isGameOver()){
                foundPath = true;
                heuValue = -depth;
                finalStep = 0;
                resultAction = tempActions.get(finalStep++);
            }else{
                //判断是否形成回路,如果没有就继续搜索
                boolean isLoop = false;
                for (StateObservation s: stateArrary){
                    if (s.equalPosition(stCopy)){
                        isLoop = true;
                        break;
                    }
                }
                if (!isLoop && depth > 0){
                    DFS(stCopy, depth -1);
                }
            }
            if (foundPath) break;
            tempActions.remove(tempActions.size()-1);
        }
        //stateArrary.remove(stCopy);
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
        if (!foundPath) {
            heuValue = MAX_HEU;
            tempActions.clear();
            stateArrary.clear();
            DFS(stateObs, LIMITED_DEPTH);
        }else{
            resultAction = tempActions.get(finalStep++);
        }
        //return resultActions.remove(resultActions.size()-1);
        //System.out.println(resultAction.toString());
        return resultAction;
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
