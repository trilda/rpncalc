/******************************************************************************
 * rpn.java
 * 
 * A simple console based RPN calculator with an optional persistent stack.
 * 
 *  Written by Michael Fross.  Copyright 2011-2019.  All rights reserved.
 *  
 *  License: GNU General Public License v3.
 *           http://www.gnu.org/licenses/gpl-3.0.html
 *           
 ******************************************************************************/
package org.fross.rpn;

import java.io.Console;
import java.util.Stack;
import gnu.getopt.Getopt;

public class Main {

	// Class Constants
	public static final String VERSION = "2019-01.01";

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Console con = null;
		Stack<Double> calcStack = new Stack<Double>();
		Stack<Double> calcStack2 = new Stack<Double>();
		boolean ProcessCommandLoop = true;
		int optionEntry;

		// Display output header information
		Output.Print("+----------------------------------------------------------------------+");
		Output.Print("|                           RPN Calculator                 v" + VERSION + " |");
		Output.Print("|           Written by Michael Fross.  All rights reserved             |");
		Output.Print("|                 Enter command 'h' for help details                   |");
		Output.Print("+----------------------------------------------------------------------+");

		// Initialize the console used for command input
		con = System.console();
		if (con == null) {
			Output.Red("FATAL ERROR:  Could not initialize OS Console for data input");
			System.exit(1);
		}

		// Process Command Line Options and set flags where needed
		Getopt optG = new Getopt("DirSize", args, "Dh?");
		while ((optionEntry = optG.getopt()) != -1) {
			switch (optionEntry) {
			case 'D': // Debug Mode
				Debug.Enable();
				break;
			case '?': // Help
			case 'h':
			default:
				Help.Display();
				System.exit(0);
				break;
			}
		}

		// Display some useful information about the environment if in Debug Mode
		Debug.Print("System Information:");
		Debug.Print(" - class.path:     " + System.getProperty("java.class.path"));
		Debug.Print("  - java.home:      " + System.getProperty("java.home"));
		Debug.Print("  - java.vendor:    " + System.getProperty("java.vendor"));
		Debug.Print("  - java.version:   " + System.getProperty("java.version"));
		Debug.Print("  - os.name:        " + System.getProperty("os.name"));
		Debug.Print("  - os.version:     " + System.getProperty("os.version"));
		Debug.Print("  - os.arch:        " + System.getProperty("os.arch"));
		Debug.Print("  - user.name:      " + System.getProperty("user.name"));
		Debug.Print("  - user.home:      " + System.getProperty("user.home"));
		Debug.Print("  - user.dir:       " + System.getProperty("user.dir"));
		Debug.Print("  - file.separator: " + System.getProperty("file.separator"));
		Debug.Print("  - library.path:   " + System.getProperty("java.library.path"));
		Debug.Print("\nCommand Line Options");
		Debug.Print("  -D:  " + Debug.Query());

		// Pull the existing stack from the preferences if they exist
		calcStack = Prefs.RestoreStack();

		// Start Main Command Loop
		while (ProcessCommandLoop == true) {
			String cmdInput = null;

			// Display the current stack
			for (int i = 0; i <= calcStack.size() - 1; i++) {
				Output.Yellow(":  " + Math.Comma(calcStack.get(i)));
			}

			// Input command/number from user
			cmdInput = con.readLine("\n>> ");

			// Process Help
			if (cmdInput.matches("^[Hh?]")) {
				Debug.Print("Displaying Help");
				Help.Display();

				// Process Exit
			} else if (cmdInput.matches("^[Xx]")) {
				Debug.Print("Exiting Command Loop");
				ProcessCommandLoop = false;

				// Process Clear
			} else if (cmdInput.matches("^[Cc]")) {
				Debug.Print("Clearing Stack");
				calcStack.clear();

				// Delete last stack item
			} else if (cmdInput.matches("^[Dd]")) {
				Debug.Print("Deleting Last Stack Item");
				try {
					if (!calcStack.isEmpty())
						calcStack.pop();
				} catch (Exception e) {
					Output.Red("ERROR: Could not delete last stack item");
					Output.Red(e.getMessage());
				}

				// Flip lasts two elements on the stack
			} else if (cmdInput.matches("^[Ff]")) {
				Debug.Print("Flipping last two elements in the stack");

				if (calcStack.size() < 2) {
					Output.Red("ERROR:  Two elements are needed for flip");
				} else {
					Double temp1 = calcStack.pop();
					Double temp2 = calcStack.pop();
					calcStack.push(temp1);
					calcStack.push(temp2);
				}

				// Change sign of last stack element
			} else if (cmdInput.matches("^[Ss]")) {
				Debug.Print("Changing sign of last stack element");
				if (!calcStack.isEmpty())
					calcStack.push(calcStack.pop() * -1);

				// Save Stack to secondary stack and clear primary
			} else if (cmdInput.matches("^[Ss][Ss]")) {
				Debug.Print("Moving primary stack to secondary");
				calcStack2 = (Stack<Double>) calcStack.clone();
				calcStack.clear();

				// Restore secondary stack to primary
			} else if (cmdInput.matches("^[Rr][Ss]")) {
				Debug.Print("Restoring secondary stack to primary");
				calcStack = (Stack<Double>) calcStack2.clone();
				calcStack2.clear();

				// Operand entered
			} else if (cmdInput.matches("[\\*\\+\\-\\/\\^]")) {
				Debug.Print("Operand entered: '" + cmdInput.charAt(0) + "'");
				calcStack = Math.Parse(cmdInput.charAt(0), calcStack);

				// Number entered, add to stack. Blank line will trigger so skip if !blank
			} else if (!cmdInput.isEmpty() && cmdInput.matches("^-?\\d*\\.?\\d*")) {
				Debug.Print("Adding entered number onto the stack");
				calcStack.push(Double.valueOf(cmdInput));

				// Handle numbers with a single operand at the end (a NumOp)
			} else if (cmdInput.matches("^-?\\d*(\\.)?\\d* ?[\\*\\+\\-\\/\\^]")) {
				char TempOp = cmdInput.charAt(cmdInput.length() - 1);
				String TempNum = cmdInput.substring(0, cmdInput.length() - 1);
				Debug.Print("NumOp Found: Op = '" + TempOp + "'");
				Debug.Print("NumOp Found: Num= '" + TempNum + "'");
				calcStack.push(Double.valueOf(TempNum));
				calcStack = Math.Parse(TempOp, calcStack);

				// Display an error if the entry was not understood
			} else {
				Output.Red("Input Error: '" + cmdInput + "'");
			}

			Output.Cyan("+----------------------------------------------------------------------+");
		}

		// Save preferences
		Prefs.SaveStack(calcStack);
	}
}
