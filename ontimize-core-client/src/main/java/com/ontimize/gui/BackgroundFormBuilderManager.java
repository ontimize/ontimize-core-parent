package com.ontimize.gui;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackgroundFormBuilderManager {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundFormBuilderManager.class);

    public static int runningThreadNumber = 4;

    protected static Vector waitingList = new Vector();

    protected static Vector runningList = new Vector();

    protected static class DetailFormBuilderThread extends Thread {

        protected IBackgroundFormBuilder backgroundBuilder;

        DetailFormBuilderThread(IBackgroundFormBuilder backgroundFormBuilder) {
            super("IBackgroundFormBuilder:" + backgroundFormBuilder.getFormName());
            this.backgroundBuilder = backgroundFormBuilder;
        }

        @Override
        public void run() {
            super.run();
            try {
                this.backgroundBuilder.getForm();
            } catch (Exception e) {
                BackgroundFormBuilderManager.logger.error(this.getClass().getName()
                        + ": Thread error building detail form -> " + this.backgroundBuilder.getFormName(), e);
            } finally {
                BackgroundFormBuilderManager.finishThread(this);
                this.backgroundBuilder = null;
            }
        }

    }

    public static void finishThread(Thread th) {
        synchronized (BackgroundFormBuilderManager.runningList) {
            BackgroundFormBuilderManager.runningList.remove(th);
            if (BackgroundFormBuilderManager.waitingList.size() > 0) {
                DetailFormBuilderThread thread = (DetailFormBuilderThread) BackgroundFormBuilderManager.waitingList
                    .remove(0);
                BackgroundFormBuilderManager.runningList.add(thread);
                thread.start();
            }
        }
    }

    public static void buildDetailForm(IBackgroundFormBuilder builder) {
        synchronized (BackgroundFormBuilderManager.runningList) {
            DetailFormBuilderThread thread = new DetailFormBuilderThread(builder);
            thread.setPriority(Thread.MIN_PRIORITY);
            if (BackgroundFormBuilderManager.runningList.size() < BackgroundFormBuilderManager.runningThreadNumber) {
                BackgroundFormBuilderManager.runningList.add(thread);
                thread.start();
            } else {
                BackgroundFormBuilderManager.waitingList.add(thread);
            }
        }
    }

}
