package Mobile;
import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.util.*;
/**
* Mobile.Place is the our mobile-agent execution platform that accepts an
* agent transferred by Mobile.Agent.hop( ), deserializes it, and resumes it
* as an independent thread.
*
* @author Swetha Kumar
* @version %I% %G$
* @since 1.0
*/
public class Place extends UnicastRemoteObject implements PlaceInterface {
private AgentLoader loader = null; // a loader to define a new agent class
private int agentSequencer = 0; // a sequencer to give a unique agentId
private ArrayList<String[]> aList = new ArrayList<String[]>();
/**
* This constructor instantiates a Mobiel.AgentLoader object that
* is used to define a new agen class coming from remotely.
*/
public Place( ) throws RemoteException {
super( );
loader = new AgentLoader( );
}
/**
* put( ) puts the message in the message Queue.
*
* @param a string array.
*/
public void put(String[] message){
aList.add(message);
System.out.println("Message Queue size after put: " + aList.size());
}
/**
* get() retrieves a message from the message queue
*
* returns a string array.
*/
public String[] get(){
String[] temp = aList.remove(0);
System.out.println("Message Queue size after get: " + aList.size());
return temp;
}
/**
* deserialize( ) deserializes a given byte array into a new agent.
*
* @param buf a byte array to be deserialized into a new Agent object.* @return a deserialized Agent object
*/
private Agent deserialize( byte[] buf )
throws IOException, ClassNotFoundException {
// converts buf into an input stream
ByteArrayInputStream in = new ByteArrayInputStream( buf );
// AgentInputStream identify a new agent class and deserialize
// a ByteArrayInputStream into a new object
AgentInputStream input = new AgentInputStream( in, loader );
return ( Agent )input.readObject();
}
/**
* transfer( ) accepts an incoming agent and launches it as an independent
* thread.
*
* @param classname The class name of an agent to be transferred.
* @param bytecode The byte code of an agent to be transferred.
* @param entity The serialized object of an agent to be transferred.
* @return true if an agent was accepted in success, otherwise false.
*/
public boolean transfer( String classname, byte[] bytecode, byte[] entity )
throws RemoteException {
boolean done = false;
try{
//Load the class
Class nClass = loader.loadClass(classname, bytecode);
//deserialize this agent
Agent agent = this.deserialize(entity);
synchronized(this){
//set Agent id
int addr =
Integer.parseInt(InetAddress.getLocalHost().getHostAddress().replace(".","").substring(
0,4));
agent.setId(addr + ++agentSequencer);
}
//Creating a new thread
Thread thread = new Thread(agent);
thread.start(); // A New thread instantiated
done = true;
}
catch(Exception ex){
System.out.println("Exception from transfer(). ClassName:
"+classname);
ex.printStackTrace();
}
return done;
}
16
/**
* main( ) starts an RMI registry in local, instantiates a Mobile.Place
* agent execution platform, and registers it into the registry.
*
* @param args receives a port, (i.e., 5001-65535).
*/
public static void main( String args[] ) {
// Implement by yourself.
if(args.length == 1){
int port = Integer.parseInt(args[0]);
try{
if (port > 5000 && port < 65536){
Place.startRegistry(port); //Trying to start the registry
given the port.
Place place = new Place();
System.out.println("Place instantiated");
Naming.rebind("rmi://localhost:" + port + "/server",
place);
System.out.println("Naming rebind sucessful!!!");
}
else{
throw new IllegalArgumentException("Port number
should be between 5001-65535");
}
}
catch(Exception ex){
System.out.println("Exception from Place main() ");
ex.printStackTrace();
}
}
}
/**
* startRegistry( ) starts an RMI registry process in local to this Place.
*
* @param port the port to which this RMI should listen.
*/
private static void startRegistry( int port ) throws RemoteException {
try {
Registry registry =
LocateRegistry.getRegistry( port );
registry.list( );
}
catch ( RemoteException e ) {
Registry registry =
LocateRegistry.createRegistry( port );
}

}
}
