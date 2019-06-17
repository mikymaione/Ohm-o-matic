/*
MIT License
Copyright (c) 2019 Michele Maione
Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package OhmOMatic.Chord;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Chord
{

	public static void main(String[] args) throws Exception
	{
		// get local machine's ip 
		String local_ip = null;

		try
		{
			local_ip = InetAddress.getLocalHost().getHostAddress();
		}
		catch (UnknownHostException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// create node
		try (var m_node = new Node(new InetSocketAddress(local_ip, Integer.parseInt(args[0]))))
		{
			// determine if it's creating or joining a existing ring
			// create, contact is this node itself
			InetSocketAddress m_contact = null;

			if (args.length == 1)
			{
				m_contact = m_node.getAddress();
			}
			else if (args.length == 3)
			{
				// join, contact is another node
				m_contact = new InetSocketAddress(args[1], Integer.parseInt(args[2]));

				if (m_contact == null)
				{
					System.out.println("Cannot find address you are trying to contact. Now exit.");
					return;
				}
			}
			else
			{
				System.out.println("Wrong input. Now exit.");
				System.exit(0);
			}

			// try to join ring from contact node
			boolean successful_join = m_node.join(m_contact);

			// fail to join contact node
			if (!successful_join)
			{
				System.out.println("Cannot connect with node you are trying to contact. Now exit.");
				System.exit(0);
			}

			// print join info
			System.out.println("Joining the Chord ring.");
			System.out.println("Local IP: " + local_ip);
			m_node.printNeighbors();

			// begin to take user input, "info" or "quit"
			try (var userinput = new Scanner(System.in))
			{
				while (true)
				{
					System.out.println("\nType \"info\" to check this node's data or \n type \"quit\"to leave ring: ");

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


}