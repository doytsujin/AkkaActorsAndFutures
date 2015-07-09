package com.logicaalternativa.examples.akka.notyped05.supervisorstrategy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;

import com.logicaalternativa.examples.akka.ActorNoTypedDummyII;
import com.logicaalternativa.examples.akka.ActorNoTypedLetItCrash;
import com.logicaalternativa.examples.akka.testbase.TestBase;

public class ActorLetItCrashTestResume extends TestBase {
	
	@Test	
	public void test() throws Exception {
		
		___GIVEN( "It creates a proxy actor (ActorNoTypedLetItCrash) . It is "
				+ "added with configuration proxied actor. In this case "
				+ "supervision strategy is 'resume'. If child actor crashes, the"
				+ "exception is not propaged and the actor is not restarted" );
		

		final Props propsChild = Props.create( ActorNoTypedDummyII.class );
		
		final Props props = Props.create
								( 
									ActorNoTypedLetItCrash.class, 
									() -> new ActorNoTypedLetItCrash( 
																	propsChild,
																	"resume" 
																  ) 
											
								);
		
		final ActorRef actorRef = system.actorOf( props, "let-itcrass" );	
		
		final BooleanAnd booleanAnd = new BooleanAnd();		
		
		
		__INFO( "**************************************************************" );
		
		___WHEN(" It send a message state");
		
		final Future<Object> future1 = Patterns.ask( actorRef, "state" , 1000 );
		
		future1.onComplete( new OnComplete<Object>(){

			@Override
			public void onComplete(Throwable exception, Object arg1) throws Throwable {
				
				___THEN( "The message recibied must be equal a "
						+ "INI => AROUND_PRE_START => PRE_START "
						+ "(" + arg1 + ")" );
				
				booleanAnd.and( exception == null );
				
				booleanAnd.and( "INI => AROUND_PRE_START => PRE_START".equals(arg1)  );
				
			}
			
		}, system.dispatcher() );
		
		
		__INFO( "**************************************************************" );
		
		__INFO( "And after..." );
		
		___WHEN(" It send a message Exception that causes a exception in "
				+ "child actor");
		
		final Exception exceptionSent = new Exception( "Exception test" );
		
		Future<Object> future2 = Patterns.ask( actorRef, exceptionSent ,1000 );
		
		future2.onComplete( new OnComplete<Object>(){

			@Override
			public void onComplete(Throwable exception, Object arg1) throws Throwable {
				
				___THEN( "The excetion must be null and message has to be equals "
						+ "'I'm going to pass away'"
						+ " (exception: " + exception + ", message: "+ arg1 +")" );
				
				booleanAnd.and( exception == null );
				
				booleanAnd.and( "I'm going to pass away".equals( arg1 ) );
				
			}
			
		}, system.dispatcher() );
		
		
		__INFO( "**************************************************************" );
		
		__INFO( "And after..." );		
		
		___WHEN(" It send a message state");
		
		final Future<Object> future3 = Patterns.ask( actorRef, "state" , 1000 );
		
		final Object result3 = Await.result( future3, Duration.create( "3 second") );
		
		___THEN( "The message recibied must be equal a "
				+ "INI => AROUND_PRE_START => PRE_START => EXCEPTION "
				+ "(" + result3 + ") ");
		
		assertEquals( "INI => AROUND_PRE_START => PRE_START => EXCEPTION", result3 );
		
		
		__INFO( "**************************************************************" );
		
		__INFO("... and finally, it's going to check all the futur results (" + booleanAnd.getRes() + ")");
		
		assertEquals( true, booleanAnd.getRes() );
		
	}
	
	
}


