/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Chord.FN.Helper;
import OhmOMatic.Chord.FN.NodeLink;
import OhmOMatic.Chord.FN.Richiesta;
import OhmOMatic.ProtoBuffer.Common;

import java.util.Scanner;

public class Query
{

	public static void main(String[] args) throws Exception
	{
		// valid args
		if (args.length == 2)
		{
			// try to parse socket address from args, if fail, exit
			var localAddress = new NodeLink(args[0], Integer.parseInt(args[1]));

			if (localAddress == null)
			{
				System.out.println("Cannot find address you are trying to contact. Now exit.");
				System.exit(0);
			}

			// successfully constructed socket address of the node we are 
			// trying to contact, check if it's alive
			var response = Helper.<Common.standardRes>sendRequest(localAddress, Richiesta.Ping);

			// if it's dead, exit
			if (response == null || !response.getMsg().equals("ALIVE"))
			{
				System.out.println("\nCannot find node you are trying to contact. Now exit.\n");
				System.exit(0);
			}

			// it's alive, print connection info
			System.out.println("Connection to node " + localAddress.IP + ", port " + localAddress.port + ", position " + Helper.hexIdAndPosition(localAddress) + ".");

			// check if system is stable
			var pred = false;
			var succ = false;

			NodeLink pred_addr = Helper.requestAddress(localAddress, Richiesta.Predecessor);
			NodeLink succ_addr = Helper.requestAddress(localAddress, Richiesta.Successor);

			if (pred_addr == null || succ_addr == null)
			{
				System.out.println("The node your are contacting is disconnected. Now exit.");
				System.exit(0);
			}

			if (pred_addr.equals(localAddress))
				pred = true;
			if (succ_addr.equals(localAddress))
				succ = true;

			// we suppose the system is stable if (1) this node has both valid 
			// predecessor and successor or (2) none of them
			while (pred ^ succ)
			{
				System.out.println("Waiting for the system to be stable...");

				pred_addr = Helper.requestAddress(localAddress, Richiesta.Predecessor);
				succ_addr = Helper.requestAddress(localAddress, Richiesta.Successor);

				if (pred_addr == null || succ_addr == null)
				{
					System.out.println("The node your are contacting is disconnected. Now exit.");
					System.exit(0);
				}

				if (pred_addr.equals(localAddress))
					pred = true;
				else
					pred = false;

				if (succ_addr.equals(localAddress))
					succ = true;
				else
					succ = false;

				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
					//
				}
			}

			// begin to take user input
			try (var userinput = new Scanner(System.in))
			{
				while (true)
				{
					System.out.println("\nPlease enter your search key (or type \"quit\" to leave): ");
					var command = userinput.nextLine();

					// quit
					if (command.startsWith("quit"))
					{
						break;
					}
					else if (command.length() > 0)
					{
						// search
						long hash = Helper.hashString(command);
						System.out.println("\nHash value is " + Long.toHexString(hash));

						NodeLink result = Helper.requestAddress(localAddress, Richiesta.FindSuccessor, hash, "");

						// if fail to send request, local node is disconnected, exit
						if (result == null)
						{
							System.out.println("The node your are contacting is disconnected. Now exit.");
							break;
						}

						// print out response
						System.out.println("\nResponse from node " + localAddress.IP + ", port " + localAddress.port + ", position " + Helper.hexIdAndPosition(localAddress) + ":");
						System.out.println("Node " + result.IP + ", port " + result.port + ", position " + Helper.hexIdAndPosition(result));
					}
				}
			}
		}
		else
		{
			System.out.println("\nInvalid input. Now exit.\n");
		}
	}


}