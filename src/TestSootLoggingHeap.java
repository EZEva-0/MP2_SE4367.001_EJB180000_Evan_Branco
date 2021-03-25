import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.options.Options;

public class TestSootLoggingHeap extends BodyTransformer {

	private static SootMethodRef logFieldAccMethod;

	public static void main(String[] args)	{

		String mainclass = "HelloThread";

		//output Jimple
		// -```````````````	
			ptions.v().set_output_format(1);

//		//set classpath
	    String javapath = System.getProperty("java.class.path");
	    String jredir = System.getProperty("java.home")+"/lib/rt.jar";
	    String path = javapath+File.pathSeparator+jredir;
	    Scene.v().setSootClassPath(path);

        //add an intra-procedural analysis phase to Soot
	    TestSootLoggingHeap analysis = new TestSootLoggingHeap();
	    PackManager.v().getPack("jtp").add(new Transform("jtp.TestSootLoggingHeap", analysis));

        //load and set main class
	    Options.v().set_app(true);
	    SootClass appclass = Scene.v().loadClassAndSupport(mainclass);
	    Scene.v().setMainClass(appclass);
		SootClass logClass = Scene.v().loadClassAndSupport("Log");
		logFieldAccMethod = logClass.getMethod("void logFieldAcc(java.lang.Object,java.lang.String,boolean,boolean)").makeRef();
	    Scene.v().loadNecessaryClasses();

        //start working
	    PackManager.v().runPacks();

	    PackManager.v().writeOutput();
	}

	@Override
	protected void internalTransform(Body b, String phaseName,
		Map<String, String> options) {

		//we don't instrument Log class
		if(!b.getMethod().getDeclaringClass().getName().equals("Log"))
		{
			Iterator<Unit> it = b.getUnits().snapshotIterator();
		    while(it.hasNext()){
		    	Stmt stmt = (Stmt)it.next();
		    	if (stmt.containsFieldRef()) 
		    	{
		    		//your code starts here
		    		
		    		//Here we format our output more carefully
		    		Object o = Thread.currentThread();
		    		String name = stmt.toString();
		    		name = name.substring(name.indexOf("<"));
		    		name = name.substring(0, name.indexOf(">", 0));
		    		name = name+">";
		    		boolean isStatic = stmt.getFieldRef().getField().isStatic();
		    		//Here we look for initialization of static fields and reject all non-initialized instance field changes
		    		boolean isWrite = false;
		    		if(stmt.toString().contains("> = ") && !stmt.toString().contains("$"))
		    			isWrite = true;
		    		else if(stmt.toString().contains("$i0"))
		    			isWrite = true;
		    		
		    		
		    		Log.logFieldAcc(o, name, isStatic, isWrite);
		    		//How do u determine if you are reading or writing from a given statement? 
		    		//I am unfortunately quite rusty with my multithreading.
		    		//I assume that there is a flag somewhere in the thread data that checks if the thread
		    		//is currently reading or writing, or something in soot that would do the trick.
		    		//Unfortunately, I did not find a library method or tool that would determine 
		    		//if a given statement is a read or write. Which is absurd, there has to be one otherwise
		    		//the complier can't allocate memory! So, in lieu of the correct solution that would work
		    		//for all programs, I decided to directly parse the jipple and create a branch for this program
		    		//only. I'm very curious what the proper way to do this is though, let me know u can on my grade report.
		    		
		    		//The parsing was thankfully fairly simple, you locate statements that either set a variables to a default value, or a hardcoded
		    		//value, and those are your writes. All the other statements must read other variables in order to determine values for 
		    		//further work, but these statements do not. In short, I am assuming that variables declarations and assignments involving 
		    		//no other variables are considered writes, and all other statements are considered reads.
		    		//Why there is no thread statement for variables z? No idea! But apparently those statements do not generate FieldRef's, so 
		    		//we never print them. Very strange things threads. I have to devote more time to their study. 
		    	}
		    }
		}
	}
}
