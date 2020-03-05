package util;

import java.util.ArrayList;

public class Cut_Point {

	public static ArrayList<String> cut_in_fragments(String code)  {		
			return getFragments(code);		
	}

	private static ArrayList<String> getFragments(String code) {
		ArrayList<String> itens = new ArrayList<String>();
		String[] fragments = code.trim().split(" ");
		for (int i = 0; i < fragments.length; i++) {
            String fragment = fragments[i];
            
            if (fragment.contains("if")) {
                //method to remove all String from the fragments
                int idToCut = getPositionFinalIF(i, fragments, true);
                String completeIF = generateString(i, idToCut, fragments);
                itens.add(completeIF);
                i = idToCut;
            }else if(fragment.contains("for")){
                int idToCut = getLastPositionForFor(i, fragments);
                String completeFor = generateString(i, idToCut, fragments);
                itens.add(completeFor);
                i = idToCut;
            }else {
                //get the position to cut the fragments 
                int idToCut = getLastPositionForBasicFunction(i, fragments);
                String completeBasicFunction = generateString(i, idToCut, fragments);
                //get the complete string                
                itens.add(completeBasicFunction);
                i = idToCut;
            }
        }
		
		
		
		return itens;
	}
	
	private static int getLastPositionForFor(int initialPosition, String[] fragments) {
        //first get the name for(u)
        if (isForInitialClause(fragments[initialPosition])) {
            initialPosition++;
        }
        //second, we get the full () to complet the for. 
        return getPositionParentClose(initialPosition, fragments);
    }
	
	private static boolean isForInitialClause(String fragment) {
        if (fragment.contains("for(u)")) {
            return true;
        }
        return false;
    }
	
	
	private static int getLastPositionForBasicFunction(int initialPosition, String[] fragments) {
        int contOpen = 0, contClosed = 0;

        for (int i = initialPosition; i < fragments.length; i++) {
            String fragment = fragments[i];
            contOpen += countCaracter(fragment, "(");
            contClosed += countCaracter(fragment, ")");
            if (contOpen == contClosed) {
                return i;
            }
        }

        return fragments.length;
    }
	
	private static int countCaracter(String fragment, String toFind) {
        int total = 0;
        for (int i = 0; i < fragment.length(); i++) {
            char ch = fragment.charAt(i);
            String x1 = String.valueOf(ch);
            if (x1.equalsIgnoreCase(toFind)) {
                total = total + 1;
            }
        }
        return total;
    }
	
	private static int getPositionFinalIF(int initialPosition, String[] fragments, boolean getElse) {
        //validate complex IF's
        if(getElse == false){
            //check if it is a if inside of a if
            if(fragments[initialPosition].startsWith("(if(")){
                int post_teste = getPositionParentClose(initialPosition, fragments);
                String newIF = generateString(initialPosition, post_teste, fragments);
                newIF = newIF.substring(1, newIF.length());
                newIF = newIF.substring(0, newIF.lastIndexOf(")"));
                String[] newfragments = newIF.split(" ");
                post_teste = getPositionFinalIF(0, newfragments, true);                
                return initialPosition + post_teste;
            }
        }
        //first get the if + conditional
        if (isIfInitialClause(fragments[initialPosition])) {
            initialPosition++;
        }
        //second get the then clause
        initialPosition = getPositionParentClose(initialPosition, fragments);

        //third get the else, if exists ATTENTION
        if (getElse && ((initialPosition + 1) <= (fragments.length - 1))) {
            if (fragments[initialPosition + 1].startsWith("(")) {
                initialPosition++;
                initialPosition = getPositionParentClose(initialPosition, fragments);
            }
        }

        if (initialPosition > (fragments.length - 1)) {
            initialPosition = (fragments.length - 1);
        }
        return initialPosition;
    }
	
	private static int getPositionParentClose(int initialPosition, String[] fragments) {
        int contOpen = 0, contClosed = 0;

        for (int i = initialPosition; i < fragments.length; i++) {
            String fragment = fragments[i];
            contOpen += countCaracter(fragment, "(");
            contClosed += countCaracter(fragment, ")");
            if (contOpen == contClosed) {
                return i;
            }
        }

        return fragments.length;
    }
	
	private static String generateString(int initialPos, int finalPos, String[] fragments) {
        String fullString = "";
        if (finalPos > (fragments.length - 1)) {
            finalPos = (fragments.length - 1);
        }
        for (int i = initialPos; i <= finalPos; i++) {
            fullString += fragments[i] + " ";
        }
        return fullString.trim();
    }
	
	private static boolean isIfInitialClause(String fragment) {
        if (fragment.startsWith("(if") && fragment.contains("(") && fragment.contains(")")) {
            return true;
        } else if (fragment.startsWith("if") && fragment.contains("(") && fragment.contains(")")) {
            return true;
        } else {
            return false;
        }
    }

}
