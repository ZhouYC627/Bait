package controllers.depthfirst;

import java.awt.*;
import java.util.ArrayList;


import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

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
        resultActions = new ArrayList<>();
        stateArrary = new ArrayList<>();
        //resultActions.clear();
        grid = so.getObservationGrid();
        block_size = so.getBlockSize();
        DFS(so);
    }
    private void DFS(StateObservation so){

        stateArrary.add(so);
        ArrayList<Types.ACTIONS> actions = so.getAvailableActions();

        for (Types.ACTIONS actionTry: actions) {
            if (foundPath) break;
            StateObservation stCopy = so.copy();
            stCopy.advance(actionTry);
            if (stCopy.isGameOver()){
                //System.out.println("Step: " + stateArrary.size());
                foundPath = true;
            }else{
                //判断是否形成回路,如果没有就继续搜索
                boolean isLoop = false;
                for (StateObservation s: stateArrary){
                    if (s.equalPosition(stCopy)){
                        isLoop = true;
                        break;
                    }
                }
                if (!isLoop){
                    DFS(stCopy);
                }
            }
            if (foundPath){
                resultActions.add(actionTry);
            }
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
        /*if (!foundPath){
            DFS(stateObs);
        }*/
        return resultActions.remove(resultActions.size()-1);
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
