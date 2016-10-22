package sudoku;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Place for your code.
 */
public class SudokuSolver {

	/**
	 * @return names of the authors and their student IDs (1 per line).
	 */
	public String authors() {
		// TODO write it;
		return "Siyan Luo 29324143" +
				"\nYumeng Chen 35365148";
	}

	/**
	 * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
	 * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
	 * @return the solved Sudoku board
	 */
	public int[][] solve(int[][] board) throws Exception {
		// TODO write it;
		// Initialize 2-dimensional Array to 1-dimensional to make things easier
		int[] single_dim_board_solution = new int[81];
		int temp                 = 0 ;
		for(int r = 0;r < 9;r++){
			for(int c = 0;c < 9;c++){
				single_dim_board_solution[temp++] = board[r][c];
			}
		}

		// Initialize 2-dimensional ArrayList for the domain for every element in the 9*9 Sudoku,
		// which means every element of ArrayList element_domain is an ArrayList with 9 integers from 1-9
		ArrayList<Integer>[] element_domain = new ArrayList[81];
		for(int i=0;i<81;i++){
			element_domain[i] = new ArrayList<Integer>();
		}

		// Domain initialization
		for(int i = 0;i < single_dim_board_solution.length;i++){
			//if the value is 0, that means this is a blank to fill in the Sudoku
			if(single_dim_board_solution[i] == 0){
				//domain for the blank should be 1-9,add them into the sub-arrayList in the element_domain
				for(int j = 1; j <= 9; j++){
					element_domain[i].add(new Integer(j));
				}
			}
			else{//if the value is fixed, that means this is not a blank. So we just need to directly add it to our solution board
				element_domain[i].add(new Integer(single_dim_board_solution[i]));
			}
		}

		// do first AC
		ArcConsistencyAlgorithm(single_dim_board_solution, element_domain) ;

		// check if any domain is length 0 -- i.e. any domain that is empty
		for( int x = 0; x < 81; x++){
			if(element_domain[x].isEmpty()){
				throw new Exception( "This board is invalid." ) ;
			}
		}

		// fill board with new answers
		// may not be complete, but still
		for( int x = 0; x < 81 ; x++){
			if( ( single_dim_board_solution[x] == 0 ) && ( element_domain[x].size() == 1 )){
				single_dim_board_solution[x] = element_domain[x].get(0) ;
			}
		}

		if( is_board_solved( single_dim_board_solution )){
			// if solved, no zeros in the board
			temp = 0 ;
			for(int x = 0; x < 9; x++){
				for(int y = 0; y < 9; y++){
					board[x][y] = single_dim_board_solution[temp++] ;
				}
			}
			return board ;
		}

		// if it is not solved yet, check TDA with domain splitting
		single_dim_board_solution  = ACwithDomSplitting(single_dim_board_solution, element_domain) ;
		temp = 0 ;

		// After domain splitting, replace the value in board with single_dim_board_solution value
		if( single_dim_board_solution != null){
			for(int x = 0; x < 9; x++){
				for(int y = 0; y < 9; y++){
					board[x][y] = single_dim_board_solution[temp++] ;
				}
			}
		}
		else{
			throw new Exception( "No solution!" ) ;
		}
		return board ;
	}

	/**
	 * Performs Arc consistency with domain splitting.
	 *@param solution_board,domains
	 * @return solved Sudoku board or throw exceptions
	 */
	private int[] ACwithDomSplitting( int[] solution_board, ArrayList<Integer>[] domains) throws Exception
	{
		int splitThis                 = 0 ;
		int splitHere                 = 0 ;
		ArrayList<Integer>[] leftdom  = new ArrayList[81] ;
		ArrayList<Integer>[] rightdom = new ArrayList[81] ;
		boolean leftOK                = true;
		boolean rightOK               = true;
		boolean assigned              = false;
		boolean equal                 = true;
		int         temp              = 0 ;
		int[] sol2                    = new int[ 81 ] ;
		int[] sol1                    = new int[ 81 ] ;

		// Initialize arrays and lists
		for(int i = 0; i < 81; i++){
			sol1[i]     = solution_board[i];
			sol2[i]     = solution_board[i];
			leftdom[i]  = new ArrayList<Integer>();
			rightdom[i] = new ArrayList<Integer>();
		}

		for(int i = 0; i < 81; i++){
			copy_dom_list( leftdom[ i ] , domains[ i ] ) ;
			copy_dom_list( rightdom[ i ], domains[ i ] ) ;
		}

		// pick domain to split
		for( int x = 0; x < 81; x++){
			if( domains[x].size() > 1){
				splitThis = x ;
				assigned = true;
			}else if(domains[x].size()== 0)
				return null;
		}
		if(!assigned){
			ArcConsistencyAlgorithm(solution_board, domains);
			for(int x = 0; x < 81 ;x++){
				if( domains[x].size() == 0){
					return null;
				}
			}
			//iterate to check if every element in Sudoku is not zero.
			for( int x = 0; x < 81 ; x++){
				if( ( solution_board[x] == 0 ) && ( domains[x].size() == 1 )){
					solution_board[x] = domains[x].get(0);
				}
			}
			return solution_board;
		}

		// split domain
		// get middle.
		splitHere = domains[splitThis].size() / 2  ;
		temp = splitHere ;

		// in left domain(first half)
		while ( leftdom[splitThis].size() > splitHere){
			leftdom[splitThis].remove( leftdom[splitThis].size() - 1 ) ;
		}

		//in right domain(second half)
		while (temp>0){
			rightdom[splitThis].remove( 0 ) ;
			temp-- ;
		}

		// DAC on left domain
		ArcConsistencyAlgorithm(sol1, leftdom) ;

		// check if any domain is length 0 -- i.e. any domain that is empty
		for( int x = 0; x < 81; x++){
			if( leftdom[x].isEmpty()){
				leftOK = false;
			}
		}

		// fill board 1 with new answers
		// may not be complete
		for( int x = 0; x < 81 ; x++){
			if((sol1[ x ] == 0 ) && (leftdom[x].size() == 1 )){
				sol1[ x ] = leftdom[ x ].get(0);
			}
		}
		// do AC on right domain
		ArcConsistencyAlgorithm( sol2, rightdom ) ;
		// check if any domain is length 0: i.e. any domain that is empty
		for( int x = 0; x < 81 ; x++){
			if( rightdom[x].isEmpty()){
				rightOK = false;
			}
		}

		// fill board 2 with new answers
		// may not be complete
		for( int x = 0; x<81; x++){
			if((sol2[ x ] == 0) && (rightdom[ x ].size() == 1)){
				sol2[ x ] = rightdom[ x ].get( 0 ) ;
			}
		}

		// recursion, repeat whole thing with split domains
		if(leftOK){
			sol1 = ACwithDomSplitting( sol1, leftdom ) ;
		}
		else{
			sol1 = null ;
		}
		if(rightOK){
			sol2 = ACwithDomSplitting( sol2, rightdom ) ;
		}
		else{
			sol2 = null ;
		}

		if ( is_board_solved(sol1)  &&  is_board_solved(sol2)){
			for( int x = 0; x < sol1.length; x++){
				if( sol1[x] != sol2[x] ) equal = false ;
			}
			if(!equal){
				throw new Exception( "there is more than one solution!") ;
			}
		}
		// return proper solutions
		if( is_board_solved(sol1) )
			return sol1;
		if(is_board_solved(sol2))
			return sol2;
		return null ;
	}

	/**
	 *  Arcs.
	 *  Representing the edge from one vertex to another vertex
	 */
	public class Arcs
	{
		int current_u;
		int connected_v;

		//constructor
		public Arcs(int u, int v)
		{
			current_u = u;
			connected_v = v;
		}
	}
	/**
	 * Arc consistency algorithm
	 * if an acr<X, r(X,Y)> is not arc consistent, delete all values in dom(X) for which there is no corresponding value in dom(Y)
	 * thie removal can never rule out any solutions
	 * @param solution_board the 1d int array representing the Sudoku board. Zeros indicate unfilled cells.
	 * @params doms the 2d arraylist representing the domains of each cell in the board
	 * @return arc consistent sudoku sol
	 */
	private void ArcConsistencyAlgorithm(int[] solution_board, ArrayList<Integer>[] domains_for_elements)
	{
		LinkedList<Arcs> TDA = new LinkedList<Arcs>() ;
		ArrayList<Integer> consistent ;
		int current_u,connected_v;
		Iterator<Integer> iteration1 ;
		Iterator<Integer> iteration2;
		Integer tempOther ;
		Arcs tempArc ;
		Integer temp ;

		// fill up ToDoArcs
		for ( int i = 0; i < 81; i ++ )
			fillTDA(TDA,i);

		// Go through all the arcs in the network
		while( TDA.size() > 0 )
		{
			//get current arcs and remove it from the list
			tempArc  = TDA.remove() ;

			//my Arcs
			current_u  = tempArc.current_u;
			connected_v = tempArc.connected_v;

			consistent = new ArrayList<Integer>() ; // list of consistent domain values
			iteration1 = domains_for_elements[current_u].iterator() ; //get values of current cell

			//
			while(iteration1.hasNext()){
				temp = iteration1.next() ;
				iteration2 = domains_for_elements[connected_v].iterator();
				//if the arrayList in index connected_v is empty, that means all elements have been deleted, so it is consistent
				if (domains_for_elements[connected_v].isEmpty())
				{consistent.add(temp);}
				while(true){
					if(iteration2.hasNext()){
						//take every element in the domain for the connected_v out and loop through. Comparing it with
						//the value in domain in connected_vm, add all values that are different
						tempOther = iteration2.next();

						//Constraint 1: if this is a arc connected, one of the constraints will be two values are not equal
						if(!temp.equals(tempOther)){
							consistent.add(temp) ;
							break;
						}
					}
					else
					{break;}
				}
			}

			if(consistent.size()!= domains_for_elements[current_u].size())
			{
				// to check for AC again
				add_twice_to_tda(TDA,current_u);
				// set the new domain
				copy_dom_list(domains_for_elements[current_u], consistent) ;
			}
		}
	}

	/**
	 *  fill up the TDA
	 */
	private void fillTDA(LinkedList<Arcs> TDA, int x)
	{
		// get row and column of position x
		int r = (int) Math.floor(x/9);
		int c = x%9;

		for (int y = 0;y<9;y++){
			if (y!= r){
				TDA.add(new Arcs(x, getPos(c, y))) ;
			}
			if ( y != c ){
				TDA.add( new Arcs(x, getPos(y, r))) ;
			}
		}
		int col_block = c / 3;
		int r_b = r / 3;
		for (int m= col_block * 3 ;m < col_block * 3 + 3 ; ++m){
			for ( int n = r_b * 3; n < r_b * 3 + 3; ++n){
				if ( m != c && n != r ){
					TDA.add(new Arcs(x, getPos(m,n))) ;
				}
			}
		}
	}

	private void add_twice_to_tda(LinkedList<Arcs> TDA, int x)
	{
		int r = (int) Math.floor(x/9);
		int c = x%9;
		int col_b = c/3;
		int r_b = r/3;

		for (int y=0; y < 9;y++){
			if (y!= r){
				TDA.add( new Arcs(getPos( c, y ), x)) ;
			}
			if (y!= c){
				TDA.add(new Arcs(getPos(y, r), x)) ;
			}
		}
		for (int m = col_b*3 ; m<col_b*3+3; ++m ){
			for ( int n = r_b * 3 ; n < r_b * 3 + 3; ++n ){
				// if not the same coordinate, add to TDA
				if (m!= c && n != r){
					TDA.add(new Arcs(getPos(m, n), x)) ;
				}
			}
		}
	}

	/*
	 *  get position of the coordinate (x, y)
	 */
	private int getPos(int r, int c)
	{	int pos = 0 ;
		for( int i = 0; i < 9; i++ ){
			for( int j = 0; j < 9; j ++ ){
				if ( c == i && r == j ){
					return pos;}
				else
				{pos++;}
			}
		}
		return pos ;
	}

	private void copy_dom_list(ArrayList<Integer> olddom_list, ArrayList<Integer> newdom_list)
	{	Iterator<Integer> itr = newdom_list.iterator();
		Integer x= 0 ;
		olddom_list.clear() ;
		while( itr.hasNext() )
		{x = itr.next() ;
			olddom_list.add(x);}
	}

	/**
	 *  check if board is a solved one
	 */
	private boolean is_board_solved(int[] solution_board)
	{	if(solution_board == null)
		{return false ;}
		for(int x = 0; x < solution_board.length; x++ ){
			if( solution_board[ x ] == 0 )
			{return false;}
		}
		return true ;
	}
}
