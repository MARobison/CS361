package nfa;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import dfa.DFA;
import fa.FAInterface;
import fa.State;

/**
 * @author Monica Robison
 *
 */
public class NFA implements FAInterface, NFAInterface{
	/* set of states*/
	private Set<NFAState> states;
	/* the start state*/
	private NFAState start;
	/*the alphabet -- used only in toString() method*/
	private Set<Character> transitos;
	
	private DFA dfa = new DFA(); //Creates a new DFA
	private Set<Set<NFAState>> visited = new HashSet<Set<NFAState>>(); //Will keep track of states already traversed
	private HashSet<NFAState> setStart = new HashSet<NFAState>(); //This will keep track of the first state
	private LinkedList<HashSet<NFAState>> queue = new LinkedList<HashSet<NFAState>>(); //Used for removing the next state in the list

	
	/**
	 * Constructor for NFA
	 */
	public NFA(){
		states = new LinkedHashSet<NFAState>();
		transitos = new LinkedHashSet<Character>();
	}
	
	@Override
	public void addStartState(String name) {
			NFAState s = getState(name);
			if(s == null){
				s = new NFAState(name);
				states.add(s);
			}
			start = s;
	}

	@Override
	public void addState(String name) {
		NFAState s = new NFAState(name);
		states.add(s);
		
	}

	@Override
	public void addFinalState(String name) {
		NFAState s = new NFAState(name, true);
		states.add(s);
		
	}

	@Override
	public boolean isFinal(State s) {
		return ((NFAState)s).isFinal();
	}

	@Override
	public void addTransition(String fromState, char onSymb, String toState) {
		(getState(fromState)).addTransition(onSymb, getState(toState));
		if(!transitos.contains(onSymb) && onSymb != 'e'){
			transitos.add(onSymb);
		}	
	}

	@Override
	public State getStartState() {
		return start;
	}
	
	/** 
	 * Method for converting a NFA to a DFA
	 * @return a DFA that has been pulled from an NFA
	 */
	@Override
	public DFA getDFA() {
		boolean isThisFinal = false;
		//Check for epsilon transitions on start
		eClosure(start, setStart);
		//Add start state to set of states that will be worked with
		setStart.add(start);
		//Check for the start state to be a final state
		//add final state to DFA
		for(NFAState n : setStart){
			if(n.isFinal()){
				isThisFinal = true;
				dfa.addFinalState(setStart.toString());
			}
		}
		//add start state to dfa
		dfa.addStartState(setStart.toString());
		queue.add(setStart);
		while (!queue.isEmpty()) {
			HashSet<NFAState> current = queue.remove();
			//Check to see if the state being looked at has already been checked
			visited.add(current);			
			isThisFinal = false;
			//To look through all the transitions
			for (Character ch : transitos) {
				//Will keep track of list of states to be looked at following transitions
				HashSet<NFAState> listOfStates = new HashSet<NFAState>();
				//Check through the current states
				for (NFAState nfa : current) {
					//Grab corresponding transitions
					HashSet<NFAState> etcStates = nfa.getTo(ch);
					//Make sure the other states are not null
					if (etcStates != null) {
						//Run through the other states that are not final or a start state
						for (NFAState otherStates : etcStates) {
							//recursively check through the connecting epsilon transitions
							listOfStates = eClosure(otherStates, listOfStates);
							//add each state to our new list of DFA States
							listOfStates.add(otherStates);
							//Check to make sure that if these newly created states contain a final state
							for(NFAState reCheck: listOfStates){
								if(reCheck.isFinal()){
									isThisFinal = true;
								}
							}
						}
					}
				}
				//Boolean check to make sure the list of states is added to the dfa
			if(isThisFinal != true){
				if(!visited.contains(listOfStates) && !queue.contains(listOfStates)){
					dfa.addState(listOfStates.toString());
				}
				//add corresponding transitions to the DFA
				dfa.addTransition(current.toString(), ch, listOfStates.toString());
			}
			//Making sure to add the final states to the DFA
			else if(!visited.contains(listOfStates) && !queue.contains(listOfStates)){
				dfa.addFinalState(listOfStates.toString());
				}	
			//Add the dfa's final state's transitions to the DFA
				dfa.addTransition(current.toString(), ch, listOfStates.toString());
			if(!visited.contains(listOfStates) && !queue.contains(listOfStates)){
				queue.add(listOfStates);
			}
		}
	}
		return dfa;
}
	/**
	 * Helper method for getting epsilon transitions (recursion)
	 * @param state
	 * @param goTo
	 * @return
	 */
	private HashSet<NFAState> eClosure(NFAState state, HashSet<NFAState> goTo) {
		HashSet<NFAState> current = state.getTo('e');
		if (current != null) {
			for (NFAState nfa : current) {
				NFAState here = nfa;
				goTo.add(here);
				eClosure(here, goTo);
			}
		} else {
			current = new HashSet<NFAState>();
		}
		return goTo;
	}
	/**
	 * Check if a state with such label
	 * has already been created and returns either a new state
	 * or already created state.
	 * @param name
	 * @return
	 */
		private NFAState getState(String name){
			NFAState ret = null;
			for(NFAState s : states){
				if(s.getName().equals(name)){
					ret = s;
					break;
				}
			}
			return ret;
		}
}
