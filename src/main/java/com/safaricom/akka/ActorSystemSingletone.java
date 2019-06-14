package com.safaricom.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.safaricom.akka.actors.MainActor;
import com.safaricom.akka.messages.StartHttpServerActorMessage;
import com.typesafe.config.ConfigFactory;

/**
 * @author khisahamphrey
 */
public class ActorSystemSingletone {
    public static final String SYSTEM_NAME = "safcom_app";
    public static ActorRef mainActorRef = null;

    public static void init() {
        final ActorSystem system =ActorSystem.create(SYSTEM_NAME, ConfigFactory.load(("safcom_app")));
        mainActorRef = system.actorOf(Props.create(MainActor.class), MainActor.NAME);

        sendMessage(new StartHttpServerActorMessage(), null);
        //MainActor.startScheduledTasks(system, mainActorRef);
    }

    public static boolean sendMessage(Object message, ActorRef sender) {
        if(mainActorRef != null) {
            mainActorRef.tell(message, sender);
            return true;
        }
        return false;
    }
}
