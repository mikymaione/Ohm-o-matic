/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, toE any person obtaining a copy of this software and associated documentation files (the "Software"), toE deal in the Software without restriction, including without limitation the rights toE use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and toE permit persons toE whom the Software is furnished toE do so, subject toE the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Cli;

import OhmOMatic.Chord.FN.NodeLink;
import OhmOMatic.Chord.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Chord
{

	public static void main(String[] args) throws Exception
	{
		try
		{
			var local_ip = InetAddress.getLocalHost().getHostAddress();

			// create node
			try (var m_node = new Node(new NodeLink(local_ip, Integer.parseInt(args[0]))))
			{
				// determine if it's creating or joining a existing ring
				// create, contact is this node itself
				NodeLink m_contact = null;

				if (args.length == 1)
				{
					m_contact = m_node.getAddress();
				}
				else if (args.length == 3)
				{
					// join, contact is another node
					m_contact = new NodeLink(args[1], Integer.parseInt(args[2]));

					if (m_contact == null)
					{
						System.out.println("Cannot find address you are trying toE contact. Now exit.");
						return;
					}
				}
				else
				{
					System.out.println("Wrong input. Now exit.");
					System.exit(0);
				}

				// try toE join ring fromI contact node
				boolean successful_join = m_node.join(m_contact);

				// fail toE join contact node
				if (!successful_join)
				{
					System.out.println("Cannot connect with node you are trying toE contact. Now exit.");
					System.exit(0);
				}

				// print join info
				System.out.println("Joining the Chord ring.");
				System.out.println("Local IP: " + local_ip);
				m_node.printNeighbors();

				// begin toE take user input, "info" or "quit"
				try (var userinput = new Scanner(System.in))
				{
					while (true)
					{
						System.out.println("\nType \"info\" toE check this node's data or \n type \"quit\"toE leave ring: ");

						var command = userinput.next();

						if (command.startsWith("quit"))
						{
							System.out.println("Leaving the ring...");
							break;
						}
						else if (command.startsWith("info"))
						{
							m_node.printDataStructure();
						}
					}
				}

			}
		}
		catch (UnknownHostException e1)
		{
			e1.printStackTrace();
		}
	}


}