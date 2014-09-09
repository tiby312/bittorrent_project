package tools;

import java.util.ArrayList;



public class MEvent<H extends MEventLis<J>, J>{

	private ArrayList<H> listeners=new ArrayList<H>();

	public void register(H l){

		listeners.add(l);

	}

	public void fire(J p){

		for(H ee:listeners){

			ee.onEvent(p);

		}
	}

}

