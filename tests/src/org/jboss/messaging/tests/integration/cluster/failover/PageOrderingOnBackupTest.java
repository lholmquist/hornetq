/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005-2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.messaging.tests.integration.cluster.failover;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.messaging.core.buffers.ChannelBuffers;
import org.jboss.messaging.core.client.ClientConsumer;
import org.jboss.messaging.core.client.ClientMessage;
import org.jboss.messaging.core.client.ClientProducer;
import org.jboss.messaging.core.client.ClientSession;
import org.jboss.messaging.core.client.ClientSessionFactory;
import org.jboss.messaging.core.client.MessageHandler;
import org.jboss.messaging.core.paging.Page;
import org.jboss.messaging.core.paging.PagedMessage;
import org.jboss.messaging.core.paging.PagingManager;
import org.jboss.messaging.core.paging.impl.TestSupportPageStore;
import org.jboss.messaging.core.server.ServerMessage;
import org.jboss.messaging.utils.SimpleString;

/**
 * 
 * It validates if the messages are in the same ordering on the page system betwen the backup and live nodes.
 * 
 * This test is valid as long as we want to guarantee strict ordering on both nodes for paged messages between backup and live nodes.
 * 
 * If we change this concept anyway this test may become invalid and we would need to delete it.
 *
 * @author <a href="mailto:clebert.suconic@jboss.org">Clebert Suconic</a>
 *
 *
 */
public class PageOrderingOnBackupTest extends FailoverTestBase
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void testPageOrderingLiveAndBackupProducerOnly() throws Exception
   {
      internalTestPageOrderingLiveAndBackup(false);
   }

   public void testPageOrderingLiveAndBackupConsume() throws Exception
   {
      internalTestPageOrderingLiveAndBackup(true);
   }

   private void internalTestPageOrderingLiveAndBackup(boolean consumeMessages) throws Exception
   {
      final SimpleString threadIDKey = new SimpleString("THREAD_ID");
      final SimpleString sequenceIDKey = new SimpleString("SEQUENCE_ID");
      final SimpleString ADDRESS = new SimpleString("SOME_QUEUE");

      final int NUMBER_OF_THREADS = 100;
      final int NUMBER_OF_MESSAGES = 200;

      final int NUMBER_OF_HANDLERS = consumeMessages ? NUMBER_OF_THREADS : 0;

      setUpFailoverServers(true, 100 * 1024, 50 * 1024);

      final ClientSessionFactory factory = createFailoverFactory();

      ClientSession session = factory.createSession(false, true, true);
      for (int i = 0; i < NUMBER_OF_THREADS; i++)
      {
         session.createQueue(ADDRESS, ADDRESS.concat("-" + i), true);
      }
      session.close();

      MyHandler handlers[] = new MyHandler[NUMBER_OF_HANDLERS];

      for (int i = 0; i < handlers.length; i++)
      {
         handlers[i] = new MyHandler(factory, ADDRESS.concat("-" + i), NUMBER_OF_MESSAGES * 10);
      }

      final CountDownLatch flagAlign = new CountDownLatch(NUMBER_OF_THREADS);
      final CountDownLatch flagStart = new CountDownLatch(1);

      class ProducerThread extends Thread
      {
         Throwable e;

         final int threadID;

         ProducerThread(int threadID)
         {
            this.threadID = threadID;
         }

         public void run()
         {
            try
            {
               ClientSession session = factory.createSession(false, true, true);
               ClientProducer producer = session.createProducer(ADDRESS);

               // I want to jinx all this by having everybody start sending at the same time
               flagAlign.countDown();
               flagStart.await();

               for (int i = 0; i < NUMBER_OF_MESSAGES; i++)
               {
                  ClientMessage msg = session.createClientMessage(true);
                  msg.setBody(ChannelBuffers.wrappedBuffer(new byte[512]));
                  msg.getProperties().putIntProperty(threadIDKey, this.threadID);
                  msg.getProperties().putIntProperty(sequenceIDKey, i);
                  producer.send(msg);
               }

               session.close();

            }
            catch (Throwable e)
            {
               // System.out => Hudson/JUNIT reports
               e.printStackTrace();
               this.e = e;
            }

         }
      }

      ProducerThread threads[] = new ProducerThread[NUMBER_OF_THREADS];

      for (int i = 0; i < threads.length; i++)
      {
         threads[i] = new ProducerThread(i);
         threads[i].start();
      }

      assertTrue("Error initializing some of the threads", flagAlign.await(10, TimeUnit.SECONDS));

      flagStart.countDown();

      for (ProducerThread t : threads)
      {
         t.join();
      }

      for (ProducerThread t : threads)
      {
         if (t.e != null)
         {
            throw new Exception("Test Failed", t.e);
         }
      }

      Thread.sleep(5000);

      for (MyHandler handler : handlers)
      {
         handler.close();
         if (handler.failure != null)
         {
            throw new Exception("Failure on consumer", handler.failure);
         }
      }

      PagingManager livePagingManager = liveService.getServer().getPostOffice().getPagingManager();
      PagingManager backupPagingManager = backupService.getServer().getPostOffice().getPagingManager();

      TestSupportPageStore livePagingStore = (TestSupportPageStore)livePagingManager.getPageStore(ADDRESS);
      TestSupportPageStore backupPagingStore = (TestSupportPageStore)backupPagingManager.getPageStore(ADDRESS);

      System.out.println("Pages: " + livePagingStore.getNumberOfPages() +
                         " on backup: " +
                         backupPagingStore.getNumberOfPages());


      if (consumeMessages)
      {
         if (livePagingStore.getNumberOfPages() == backupPagingStore.getNumberOfPages() - 1)
         {
            // The live node may have one extra page in front of the backup
            backupPagingStore.depage();
         }
      }

      assertEquals(livePagingStore.getNumberOfPages(), backupPagingStore.getNumberOfPages());

      Page livePage = null;
      Page backupPage = null;

      while (true)
      {
         livePage = livePagingStore.depage();

         if (livePage == null)
         {
            assertNull(backupPagingStore.depage());
            break;
         }

         backupPage = backupPagingStore.depage();

         assertNotNull(backupPage);

         livePage.open();
         backupPage.open();

         List<PagedMessage> liveMessages = livePage.read();
         List<PagedMessage> backupMessages = backupPage.read();

         livePage.close();
         backupPage.close();

         assertEquals(liveMessages.size(), backupMessages.size());

         Iterator<PagedMessage> backupIterator = backupMessages.iterator();

         for (PagedMessage liveMsg : liveMessages)
         {
            PagedMessage backupMsg = backupIterator.next();
            assertNotNull(backupMsg);

            ServerMessage liveSrvMsg = liveMsg.getMessage(null);
            ServerMessage backupSrvMsg = liveMsg.getMessage(null);

            assertEquals(liveSrvMsg.getMessageID(), backupSrvMsg.getMessageID());
            assertEquals(liveSrvMsg.getProperty(threadIDKey), backupSrvMsg.getProperty(threadIDKey));
            assertEquals(liveSrvMsg.getProperty(sequenceIDKey), backupSrvMsg.getProperty(sequenceIDKey));
         }
      }

   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

   class MyHandler implements MessageHandler
   {
      final ClientSession session;

      final ClientConsumer consumer;

      volatile boolean started = true;

      final int msgs;

      volatile int receivedMsgs = 0;

      final CountDownLatch latch;

      Throwable failure;

      MyHandler(ClientSessionFactory sf, SimpleString address, final int msgs) throws Exception
      {
         this.session = sf.createSession(null, null, false, true, true, false, 0);
         this.consumer = session.createConsumer(address);
         consumer.setMessageHandler(this);
         this.session.start();
         this.msgs = msgs;
         latch = new CountDownLatch(msgs);
      }

      public synchronized void close() throws Exception
      {
         session.close();
      }

      /* (non-Javadoc)
       * @see org.jboss.messaging.core.client.MessageHandler#onMessage(org.jboss.messaging.core.client.ClientMessage)
       */
      public synchronized void onMessage(ClientMessage message)
      {
         try
         {
            if (!started)
            {
               throw new IllegalStateException("Stopped Handler received message");
            }

            if (receivedMsgs++ == msgs)
            {
               System.out.println("done");
               started = false;
               session.stop();
            }

            message.acknowledge();

            if (!started)
            {
               latch.countDown();
            }

         }
         catch (Throwable e)
         {
            this.failure = e;
         }
      }

   }
}
