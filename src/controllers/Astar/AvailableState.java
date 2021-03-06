package controllers.Astar;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by answer on 3/14/17.
 */
public class AvailableState implements Comparable{

    StateObservation stateObs;
    public double costs;
    public double heuValue;
    private int steps;
    public AvailableState lastState;

    private static final double MAX_HEU = Double.MAX_VALUE;

    public AvailableState(StateObservation so){
        stateObs = so.copy();
        costs = 0;
        steps = 0;
        lastState = null;
        heuValue = heuristic(stateObs);
    }

    public AvailableState(StateObservation so, AvailableState last){
        this.lastState = last;
        this.steps = last.getSteps() + 1;
        stateObs = so.copy();
        //costs = steps * 50;
        heuValue = heuristic(stateObs);
    }

    public int getSteps(){
        return steps;
    }

    private double gridDist(Vector2d v0, Vector2d v1){
        return Math.abs(v0.x-v1.x)+Math.abs(v0.y-v1.y);
    }

    private double heuristic(StateObservation stateObs){
        costs = steps * 50;
        if(stateObs.isGameOver()) {
            if (stateObs.getGameWinner() == Types.WINNER.PLAYER_WINS) {
                return 0;
            }else{
                return MAX_HEU;
            }
        }
        ArrayList<Observation>[] fixedPositions = stateObs.getImmovablePositions();
        ArrayList<Observation>[] movingPositions = stateObs.getMovablePositions();
        Vector2d goalpos = new Vector2d(0,0) ;
        for (ArrayList<Observation> fixedPos : fixedPositions){
            int iType = fixedPos.get(0).itype;
            switch (iType){
                //Hole
                case 2:
                    costs += 500 * fixedPos.size();
                    break;
                //Mushroom
                case 5:
                    costs += 100 * fixedPos.size();
                    break;
                //Goal
                case 7:
                    goalpos = fixedPos.get(0).position;
                    break;
            }
        }
        Vector2d avatarpos = stateObs.getAvatarPosition();
        Vector2d keypos;

        double dist = MAX_HEU;
        if (stateObs.getAvatarType()==1){//没拿到钥匙
            keypos = movingPositions[0].get(0).position;
            dist = gridDist(avatarpos, keypos) + gridDist(keypos, goalpos) + 500;
        }else if(stateObs.getAvatarType()==4){//拿到了钥匙
            dist = gridDist(avatarpos, goalpos);
        }
        return dist + costs;
    }

    @Override
    public int compareTo(Object o) {
        return (int)(this.heuValue - ((AvailableState)o).heuValue);
    }
}
