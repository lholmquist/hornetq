/*
 * Copyright 2009 Red Hat, Inc.
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.hornetq.tests.stress.failover;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.hornetq.api.core.HornetQException;
import org.hornetq.api.core.SimpleString;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.*;
import org.hornetq.core.client.impl.ClientSessionFactoryImpl;
import org.hornetq.core.client.impl.ClientSessionInternal;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.logging.Logger;
import org.hornetq.core.remoting.impl.invm.InVMRegistry;
import org.hornetq.core.server.HornetQServer;
import org.hornetq.core.server.HornetQServers;
import org.hornetq.jms.client.HornetQTextMessage;
import org.hornetq.tests.util.ServiceTestBase;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * A RandomFailoverStressTest
 *
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 * 
 * Created Jan 22, 2009 8:32:59 PM
 *
 *
 */
public class RandomReattachStressTest extends ServiceTestBase
{

   private static final Logger log = Logger.getLogger(RandomReattachStressTest.class);

   // Constants -----------------------------------------------------

   private static final int RECEIVE_TIMEOUT = 10000;

   // Attributes ----------------------------------------------------

   private static final SimpleString ADDRESS = new SimpleString("FailoverTestAddress");

   private HornetQServer liveService;

   private Timer timer;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   public void testA() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestA(sf);
         }
      });
   }

   public void testB() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestB(sf);
         }
      });
   }

   public void testC() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestC(sf);
         }
      });
   }

   public void testD() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestD(sf);
         }
      });
   }

   public void testE() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestE(sf);
         }
      });
   }

   public void testF() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestF(sf);
         }
      });
   }

   public void testG() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestG(sf);
         }
      });
   }

   public void testH() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestH(sf);
         }
      });
   }

   public void testI() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestI(sf);
         }
      });
   }

   public void testJ() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestJ(sf);
         }
      });
   }

   public void testK() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestK(sf);
         }
      });
   }

   public void testL() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestL(sf);
         }
      });
   }

   public void testN() throws Exception
   {
      runTest(new RunnableT()
      {
         @Override
         public void run(final ClientSessionFactory sf) throws Exception
         {
            doTestN(sf);
         }
      });
   }

   public void runTest(final RunnableT runnable) throws Exception
   {
      final int numIts = getNumIterations();

      for (int its = 0; its < numIts; its++)
      {
         RandomReattachStressTest.log.info("####" + getName() + " iteration #" + its);
         start();
         ServerLocator locator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(ServiceTestBase.INVM_CONNECTOR_FACTORY));

         locator.setReconnectAttempts(-1);
         locator.setConfirmationWindowSize(1024 * 1024);

         ClientSessionFactoryImpl sf = (ClientSessionFactoryImpl) locator.createSessionFactory();


         ClientSession session = sf.createSession(false, false, false);

         Failer failer = startFailer(1000, session);

         do
         {
            runnable.run(sf);
         }
         while (!failer.isExecuted());

         session.close();

         locator.close();

         Assert.assertEquals(0, sf.numSessions());

         Assert.assertEquals(0, sf.numConnections());

         stop();
      }
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   protected void doTestA(final ClientSessionFactory sf) throws Exception
   {
      long start = System.currentTimeMillis();

      ClientSession s = sf.createSession(false, false, false);

      final int numMessages = 100;

      final int numSessions = 10;

      Set<ClientConsumer> consumers = new HashSet<ClientConsumer>();
      Set<ClientSession> sessions = new HashSet<ClientSession>();

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         ClientSession sessConsume = sf.createSession(false, true, true);

         sessConsume.start();

         sessConsume.createQueue(RandomReattachStressTest.ADDRESS, subName, null, false);

         ClientConsumer consumer = sessConsume.createConsumer(subName);

         consumers.add(consumer);

         sessions.add(sessConsume);
      }

      ClientSession sessSend = sf.createSession(false, true, true);

      ClientProducer producer = sessSend.createProducer(RandomReattachStressTest.ADDRESS);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      class MyHandler extends AssertionCheckMessageHandler
      {
         final CountDownLatch latch = new CountDownLatch(1);

         volatile int count;

         public void onMessageAssert(final ClientMessage message)
         {
            if (count == numMessages)
            {
               Assert.fail("Too many messages");
            }

            Assert.assertEquals(count, message.getObjectProperty(new SimpleString("count")));

            count++;

            try
            {
               message.acknowledge();
            }
            catch (HornetQException me)
            {
               RandomReattachStressTest.log.error("Failed to process", me);
            }

            if (count == numMessages)
            {
               latch.countDown();
            }
         }
      }

      Set<MyHandler> handlers = new HashSet<MyHandler>();

      for (ClientConsumer consumer : consumers)
      {
         MyHandler handler = new MyHandler();

         consumer.setMessageHandler(handler);

         handlers.add(handler);
      }

      for (MyHandler handler : handlers)
      {
         boolean ok = handler.latch.await(5000, TimeUnit.MILLISECONDS);
         
         handler.checkAssertions();

         Assert.assertTrue("Didn't receive all messages", ok);
      }

      sessSend.close();
      for (ClientSession session : sessions)
      {
         session.close();
      }

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         s.deleteQueue(subName);
      }

      s.close();

      long end = System.currentTimeMillis();

      RandomReattachStressTest.log.info("duration " + (end - start));
   }

   protected void doTestB(final ClientSessionFactory sf) throws Exception
   {
      long start = System.currentTimeMillis();

      ClientSession s = sf.createSession(false, false, false);

      final int numMessages = 100;

      final int numSessions = 50;

      Set<ClientConsumer> consumers = new HashSet<ClientConsumer>();
      Set<ClientSession> sessions = new HashSet<ClientSession>();

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         ClientSession sessConsume = sf.createSession(false, true, true);

         sessConsume.createQueue(RandomReattachStressTest.ADDRESS, subName, null, false);

         ClientConsumer consumer = sessConsume.createConsumer(subName);

         consumers.add(consumer);

         sessions.add(sessConsume);
      }

      ClientSession sessSend = sf.createSession(false, true, true);

      ClientProducer producer = sessSend.createProducer(RandomReattachStressTest.ADDRESS);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      for (ClientSession session : sessions)
      {
         session.start();
      }

      class MyHandler extends AssertionCheckMessageHandler
      {
         final CountDownLatch latch = new CountDownLatch(1);

         volatile int count;

         public void onMessageAssert(final ClientMessage message)
         {
            if (count == numMessages)
            {
               Assert.fail("Too many messages");
            }

            Assert.assertEquals(count, message.getObjectProperty(new SimpleString("count")));

            count++;

            if (count == numMessages)
            {
               latch.countDown();
            }
         }
      }

      Set<MyHandler> handlers = new HashSet<MyHandler>();

      for (ClientConsumer consumer : consumers)
      {
         MyHandler handler = new MyHandler();

         consumer.setMessageHandler(handler);

         handlers.add(handler);
      }

      for (MyHandler handler : handlers)
      {
         boolean ok = handler.latch.await(10000, TimeUnit.MILLISECONDS);
         
         handler.checkAssertions();

         Assert.assertTrue(ok);
      }

      sessSend.close();

      for (ClientSession session : sessions)
      {
         session.close();
      }

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         s.deleteQueue(subName);
      }

      s.close();

      long end = System.currentTimeMillis();

      RandomReattachStressTest.log.info("duration " + (end - start));

   }

   protected void doTestC(final ClientSessionFactory sf) throws Exception
   {
      long start = System.currentTimeMillis();

      ClientSession s = sf.createSession(false, false, false);

      final int numMessages = 100;

      final int numSessions = 1;

      Set<ClientConsumer> consumers = new HashSet<ClientConsumer>();
      Set<ClientSession> sessions = new HashSet<ClientSession>();

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         ClientSession sessConsume = sf.createSession(false, false, false);

         sessConsume.start();

         sessConsume.createQueue(RandomReattachStressTest.ADDRESS, subName, null, false);

         ClientConsumer consumer = sessConsume.createConsumer(subName);

         consumers.add(consumer);

         sessions.add(sessConsume);
      }

      ClientSession sessSend = sf.createSession(false, false, true);

      ClientProducer producer = sessSend.createProducer(RandomReattachStressTest.ADDRESS);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      sessSend.rollback();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      sessSend.commit();

      class MyHandler extends AssertionCheckMessageHandler
      {
         final CountDownLatch latch = new CountDownLatch(1);

         volatile int count;

         public void onMessageAssert(final ClientMessage message)
         {
            if (count == numMessages)
            {
               Assert.fail("Too many messages, expected " + count);
            }

            Assert.assertEquals(count, message.getObjectProperty(new SimpleString("count")));

            count++;
            
            try
            {
               message.acknowledge();
            }
            catch (HornetQException e)
            {
               e.printStackTrace();
               throw new RuntimeException (e.getMessage(), e);
            }

            if (count == numMessages)
            {
               latch.countDown();
            }
         }
      }

      Set<MyHandler> handlers = new HashSet<MyHandler>();

      for (ClientConsumer consumer : consumers)
      {
         MyHandler handler = new MyHandler();

         consumer.setMessageHandler(handler);

         handlers.add(handler);
      }

      for (MyHandler handler : handlers)
      {
         boolean ok = handler.latch.await(10000, TimeUnit.MILLISECONDS);

         Assert.assertTrue(ok);
         
         handler.checkAssertions();
      }

      handlers.clear();

      // New handlers
      for (ClientConsumer consumer : consumers)
      {
         MyHandler handler = new MyHandler();

         consumer.setMessageHandler(handler);

         handlers.add(handler);
      }

      for (ClientSession session : sessions)
      {
         session.rollback();
      }

      for (MyHandler handler : handlers)
      {
         boolean ok = handler.latch.await(10000, TimeUnit.MILLISECONDS);

         Assert.assertTrue(ok);
         
         handler.checkAssertions();
      }

      for (ClientSession session : sessions)
      {
         session.commit();
      }

      sessSend.close();
      for (ClientSession session : sessions)
      {
         session.close();
      }

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         s.deleteQueue(subName);
      }

      s.close();

      long end = System.currentTimeMillis();

      RandomReattachStressTest.log.info("duration " + (end - start));
   }

   protected void doTestD(final ClientSessionFactory sf) throws Exception
   {
      long start = System.currentTimeMillis();

      ClientSession s = sf.createSession(false, false, false);

      final int numMessages = 100;

      final int numSessions = 10;

      Set<ClientConsumer> consumers = new HashSet<ClientConsumer>();
      Set<ClientSession> sessions = new HashSet<ClientSession>();

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         ClientSession sessConsume = sf.createSession(false, false, false);

         sessConsume.createQueue(RandomReattachStressTest.ADDRESS, subName, null, false);

         ClientConsumer consumer = sessConsume.createConsumer(subName);

         consumers.add(consumer);

         sessions.add(sessConsume);
      }

      ClientSession sessSend = sf.createSession(false, false, true);

      ClientProducer producer = sessSend.createProducer(RandomReattachStressTest.ADDRESS);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      sessSend.rollback();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      sessSend.commit();

      for (ClientSession session : sessions)
      {
         session.start();
      }

      class MyHandler extends AssertionCheckMessageHandler
      {
         final CountDownLatch latch = new CountDownLatch(1);

         volatile int count;

         public void onMessageAssert(final ClientMessage message)
         {
            if (count == numMessages)
            {
               Assert.fail("Too many messages, " + count);
            }

            Assert.assertEquals(count, message.getObjectProperty(new SimpleString("count")));

            count++;

            if (count == numMessages)
            {
               latch.countDown();
            }
         }
      }

      Set<MyHandler> handlers = new HashSet<MyHandler>();

      for (ClientConsumer consumer : consumers)
      {
         MyHandler handler = new MyHandler();

         consumer.setMessageHandler(handler);

         handlers.add(handler);
      }

      for (MyHandler handler : handlers)
      {
         boolean ok = handler.latch.await(20000, TimeUnit.MILLISECONDS);

         Assert.assertTrue(ok);
         
         handler.checkAssertions();
      }

      handlers.clear();

      // New handlers
      for (ClientConsumer consumer : consumers)
      {
         MyHandler handler = new MyHandler();

         consumer.setMessageHandler(handler);

         handlers.add(handler);
      }

      for (ClientSession session : sessions)
      {
         session.rollback();
      }

      for (MyHandler handler : handlers)
      {
         boolean ok = handler.latch.await(10000, TimeUnit.MILLISECONDS);

         Assert.assertTrue(ok);
         
         handler.checkAssertions();
      }

      for (ClientSession session : sessions)
      {
         session.commit();
      }

      sessSend.close();
      for (ClientSession session : sessions)
      {
         session.close();
      }

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         s.deleteQueue(subName);
      }

      s.close();

      long end = System.currentTimeMillis();

      RandomReattachStressTest.log.info("duration " + (end - start));
   }

   // Now with synchronous receive()

   protected void doTestE(final ClientSessionFactory sf) throws Exception
   {
      long start = System.currentTimeMillis();

      ClientSession s = sf.createSession(false, false, false);

      final int numMessages = 100;

      final int numSessions = 10;

      Set<ClientConsumer> consumers = new HashSet<ClientConsumer>();
      Set<ClientSession> sessions = new HashSet<ClientSession>();

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         ClientSession sessConsume = sf.createSession(false, true, true);

         sessConsume.start();

         sessConsume.createQueue(RandomReattachStressTest.ADDRESS, subName, null, false);

         ClientConsumer consumer = sessConsume.createConsumer(subName);

         consumers.add(consumer);

         sessions.add(sessConsume);
      }

      ClientSession sessSend = sf.createSession(false, true, true);

      ClientProducer producer = sessSend.createProducer(RandomReattachStressTest.ADDRESS);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receive(RandomReattachStressTest.RECEIVE_TIMEOUT);

            Assert.assertNotNull(msg);

            Assert.assertEquals(i, msg.getObjectProperty(new SimpleString("count")));

            msg.acknowledge();
         }
      }

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receiveImmediate();

            Assert.assertNull(msg);
         }
      }

      sessSend.close();
      for (ClientSession session : sessions)
      {
         session.close();
      }

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         s.deleteQueue(subName);
      }

      s.close();

      long end = System.currentTimeMillis();

      RandomReattachStressTest.log.info("duration " + (end - start));
   }

   protected void doTestF(final ClientSessionFactory sf) throws Exception
   {
      long start = System.currentTimeMillis();

      ClientSession s = sf.createSession(false, false, false);

      final int numMessages = 100;

      final int numSessions = 10;

      Set<ClientConsumer> consumers = new HashSet<ClientConsumer>();
      Set<ClientSession> sessions = new HashSet<ClientSession>();

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         ClientSession sessConsume = sf.createSession(false, true, true);

         sessConsume.createQueue(RandomReattachStressTest.ADDRESS, subName, null, false);

         ClientConsumer consumer = sessConsume.createConsumer(subName);

         consumers.add(consumer);

         sessions.add(sessConsume);
      }

      ClientSession sessSend = sf.createSession(false, true, true);

      ClientProducer producer = sessSend.createProducer(RandomReattachStressTest.ADDRESS);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      for (ClientSession session : sessions)
      {
         session.start();
      }

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receive(RandomReattachStressTest.RECEIVE_TIMEOUT);

            if (msg == null)
            {
               throw new IllegalStateException("Failed to receive message " + i);
            }

            Assert.assertNotNull(msg);

            Assert.assertEquals(i, msg.getObjectProperty(new SimpleString("count")));

            msg.acknowledge();
         }
      }

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receiveImmediate();

            Assert.assertNull(msg);
         }
      }

      sessSend.close();
      for (ClientSession session : sessions)
      {
         session.close();
      }

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         s.deleteQueue(subName);
      }

      s.close();

      Assert.assertEquals(1, ((ClientSessionFactoryImpl)sf).numSessions());

      long end = System.currentTimeMillis();

      RandomReattachStressTest.log.info("duration " + (end - start));
   }

   protected void doTestG(final ClientSessionFactory sf) throws Exception
   {
      long start = System.currentTimeMillis();

      ClientSession s = sf.createSession(false, false, false);

      final int numMessages = 100;

      final int numSessions = 10;

      Set<ClientConsumer> consumers = new HashSet<ClientConsumer>();
      Set<ClientSession> sessions = new HashSet<ClientSession>();

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         ClientSession sessConsume = sf.createSession(false, false, false);

         sessConsume.start();

         sessConsume.createQueue(RandomReattachStressTest.ADDRESS, subName, null, false);

         ClientConsumer consumer = sessConsume.createConsumer(subName);

         consumers.add(consumer);

         sessions.add(sessConsume);
      }

      ClientSession sessSend = sf.createSession(false, false, false);

      ClientProducer producer = sessSend.createProducer(RandomReattachStressTest.ADDRESS);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      sessSend.rollback();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      sessSend.commit();

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receive(RandomReattachStressTest.RECEIVE_TIMEOUT);

            Assert.assertNotNull(msg);

            Assert.assertEquals(i, msg.getObjectProperty(new SimpleString("count")));

            msg.acknowledge();
         }
      }

      for (ClientConsumer consumer : consumers)
      {
         ClientMessage msg = consumer.receiveImmediate();

         Assert.assertNull(msg);
      }

      for (ClientSession session : sessions)
      {
         session.rollback();
      }

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receive(RandomReattachStressTest.RECEIVE_TIMEOUT);

            Assert.assertNotNull(msg);

            Assert.assertEquals(i, msg.getObjectProperty(new SimpleString("count")));

            msg.acknowledge();
         }
      }

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receiveImmediate();

            Assert.assertNull(msg);
         }
      }

      for (ClientSession session : sessions)
      {
         session.commit();
      }

      sessSend.close();
      for (ClientSession session : sessions)
      {
         session.close();
      }

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         s.deleteQueue(subName);
      }

      s.close();

      long end = System.currentTimeMillis();

      RandomReattachStressTest.log.info("duration " + (end - start));
   }

   protected void doTestH(final ClientSessionFactory sf) throws Exception
   {
      long start = System.currentTimeMillis();

      ClientSession s = sf.createSession(false, false, false);

      final int numMessages = 100;

      final int numSessions = 10;

      Set<ClientConsumer> consumers = new HashSet<ClientConsumer>();
      Set<ClientSession> sessions = new HashSet<ClientSession>();

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         ClientSession sessConsume = sf.createSession(false, false, false);

         sessConsume.createQueue(RandomReattachStressTest.ADDRESS, subName, null, false);

         ClientConsumer consumer = sessConsume.createConsumer(subName);

         consumers.add(consumer);

         sessions.add(sessConsume);
      }

      ClientSession sessSend = sf.createSession(false, false, false);

      ClientProducer producer = sessSend.createProducer(RandomReattachStressTest.ADDRESS);

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      sessSend.rollback();

      for (int i = 0; i < numMessages; i++)
      {
         ClientMessage message = sessSend.createMessage(HornetQTextMessage.TYPE,
                                                              false,
                                                              0,
                                                              System.currentTimeMillis(),
                                                              (byte)1);
         message.putIntProperty(new SimpleString("count"), i);
         producer.send(message);
      }

      sessSend.commit();

      for (ClientSession session : sessions)
      {
         session.start();
      }

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receive(RandomReattachStressTest.RECEIVE_TIMEOUT);

            Assert.assertNotNull(msg);

            Assert.assertEquals(i, msg.getObjectProperty(new SimpleString("count")));

            msg.acknowledge();
         }
      }

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receiveImmediate();

            Assert.assertNull(msg);
         }
      }

      for (ClientSession session : sessions)
      {
         session.rollback();
      }

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receive(RandomReattachStressTest.RECEIVE_TIMEOUT);

            Assert.assertNotNull(msg);

            Assert.assertEquals(i, msg.getObjectProperty(new SimpleString("count")));

            msg.acknowledge();
         }
      }

      for (int i = 0; i < numMessages; i++)
      {
         for (ClientConsumer consumer : consumers)
         {
            ClientMessage msg = consumer.receiveImmediate();

            Assert.assertNull(msg);
         }
      }

      for (ClientSession session : sessions)
      {
         session.commit();
      }

      sessSend.close();
      for (ClientSession session : sessions)
      {
         session.close();
      }

      for (int i = 0; i < numSessions; i++)
      {
         SimpleString subName = new SimpleString("sub" + i);

         s.deleteQueue(subName);
      }

      s.close();

      long end = System.currentTimeMillis();

      RandomReattachStressTest.log.info("duration " + (end - start));
   }

   protected void doTestI(final ClientSessionFactory sf) throws Exception
   {
      ClientSession sessCreate = sf.createSession(false, true, true);

      sessCreate.createQueue(RandomReattachStressTest.ADDRESS, RandomReattachStressTest.ADDRESS, null, false);

      ClientSession sess = sf.createSession(false, true, true);

      sess.start();

      ClientConsumer consumer = sess.createConsumer(RandomReattachStressTest.ADDRESS);

      ClientProducer producer = sess.createProducer(RandomReattachStressTest.ADDRESS);

      ClientMessage message = sess.createMessage(HornetQTextMessage.TYPE,
                                                       false,
                                                       0,
                                                       System.currentTimeMillis(),
                                                       (byte)1);
      producer.send(message);

      ClientMessage message2 = consumer.receive(RandomReattachStressTest.RECEIVE_TIMEOUT);

      Assert.assertNotNull(message2);

      message2.acknowledge();

      sess.close();

      sessCreate.deleteQueue(RandomReattachStressTest.ADDRESS);

      sessCreate.close();
   }

   protected void doTestJ(final ClientSessionFactory sf) throws Exception
   {
      ClientSession sessCreate = sf.createSession(false, true, true);

      sessCreate.createQueue(RandomReattachStressTest.ADDRESS, RandomReattachStressTest.ADDRESS, null, false);

      ClientSession sess = sf.createSession(false, true, true);

      sess.start();

      ClientConsumer consumer = sess.createConsumer(RandomReattachStressTest.ADDRESS);

      ClientProducer producer = sess.createProducer(RandomReattachStressTest.ADDRESS);

      ClientMessage message = sess.createMessage(HornetQTextMessage.TYPE,
                                                       false,
                                                       0,
                                                       System.currentTimeMillis(),
                                                       (byte)1);
      producer.send(message);

      ClientMessage message2 = consumer.receive(RandomReattachStressTest.RECEIVE_TIMEOUT);

      Assert.assertNotNull(message2);

      message2.acknowledge();

      sess.close();

      sessCreate.deleteQueue(RandomReattachStressTest.ADDRESS);

      sessCreate.close();
   }

   protected void doTestK(final ClientSessionFactory sf) throws Exception
   {
      ClientSession s = sf.createSession(false, false, false);

      s.createQueue(RandomReattachStressTest.ADDRESS, RandomReattachStressTest.ADDRESS, null, false);

      final int numConsumers = 100;

      for (int i = 0; i < numConsumers; i++)
      {
         ClientConsumer consumer = s.createConsumer(RandomReattachStressTest.ADDRESS);

         consumer.close();
      }

      s.deleteQueue(RandomReattachStressTest.ADDRESS);

      s.close();
   }

   protected void doTestL(final ClientSessionFactory sf) throws Exception
   {
      final int numSessions = 10;

      for (int i = 0; i < numSessions; i++)
      {
         ClientSession session = sf.createSession(false, false, false);

         session.close();
      }
   }

   protected void doTestN(final ClientSessionFactory sf) throws Exception
   {
      ClientSession sessCreate = sf.createSession(false, true, true);

      sessCreate.createQueue(RandomReattachStressTest.ADDRESS,
                             new SimpleString(RandomReattachStressTest.ADDRESS.toString()),
                             null,
                             false);

      ClientSession sess = sf.createSession(false, true, true);

      sess.stop();

      sess.start();

      sess.stop();

      ClientConsumer consumer = sess.createConsumer(new SimpleString(RandomReattachStressTest.ADDRESS.toString()));

      ClientProducer producer = sess.createProducer(RandomReattachStressTest.ADDRESS);

      ClientMessage message = sess.createMessage(HornetQTextMessage.TYPE,
                                                       false,
                                                       0,
                                                       System.currentTimeMillis(),
                                                       (byte)1);
      producer.send(message);

      sess.start();

      ClientMessage message2 = consumer.receive(RandomReattachStressTest.RECEIVE_TIMEOUT);

      Assert.assertNotNull(message2);

      message2.acknowledge();

      sess.stop();

      sess.start();

      sess.close();

      sessCreate.deleteQueue(new SimpleString(RandomReattachStressTest.ADDRESS.toString()));

      sessCreate.close();
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      timer = new Timer(true);
   }

   @Override
   protected void tearDown() throws Exception
   {
      timer.cancel();

      InVMRegistry.instance.clear();

      super.tearDown();
   }

   // Private -------------------------------------------------------

   private Failer startFailer(final long time, final ClientSession session)
   {
      Failer failer = new Failer((ClientSessionInternal)session);

      timer.schedule(failer, (long)(time * Math.random()), 100);

      return failer;
   }

   private void start() throws Exception
   {
      Configuration liveConf = createDefaultConfig();
      liveConf.setSecurityEnabled(false);
      liveConf.getAcceptorConfigurations()
              .add(new TransportConfiguration("org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory"));
      liveService = HornetQServers.newHornetQServer(liveConf, false);
      liveService.start();
   }

   private void stop() throws Exception
   {
      liveService.stop();

      Assert.assertEquals(0, InVMRegistry.instance.size());

      liveService = null;
   }

   // Inner classes -------------------------------------------------

   class Failer extends TimerTask
   {
      private final ClientSessionInternal session;

      private boolean executed;

      public Failer(final ClientSessionInternal session)
      {
         this.session = session;
      }

      @Override
      public synchronized void run()
      {
         RandomReattachStressTest.log.info("** Failing connection");

         session.getConnection().fail(new HornetQException(HornetQException.NOT_CONNECTED, "oops"));

         RandomReattachStressTest.log.info("** Fail complete");

         cancel();

         executed = true;
      }

      public synchronized boolean isExecuted()
      {
         return executed;
      }
   }

   public abstract class RunnableT
   {
      abstract void run(final ClientSessionFactory sf) throws Exception;
   }
   
   static abstract class AssertionCheckMessageHandler implements MessageHandler
   {
      
      
      public void checkAssertions()
      {
         for (AssertionFailedError e: errors)
         {
            // it will throw the first error
            throw e;
         }
      }
      
      private ArrayList<AssertionFailedError> errors = new ArrayList<AssertionFailedError>(); 

      /* (non-Javadoc)
       * @see org.hornetq.api.core.client.MessageHandler#onMessage(org.hornetq.api.core.client.ClientMessage)
       */
      public void onMessage(ClientMessage message)
      {
         try
         {
            onMessageAssert(message);
         }
         catch (AssertionFailedError e)
         {
            e.printStackTrace(); // System.out -> junit reports
            errors.add(e);
         }
      }
      
      public abstract void onMessageAssert(ClientMessage message);
      
   }
   
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   protected int getNumIterations()
   {
      return 100;
   }

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}