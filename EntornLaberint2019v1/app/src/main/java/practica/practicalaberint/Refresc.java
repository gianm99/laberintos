package practica.practicalaberint;

/**
 * Created by Ramon Mas on 10/3/16.
 */

import android.graphics.Canvas;

import java.util.concurrent.Semaphore;

public class Refresc extends Thread
{
    private MainGame view;
    private boolean running = false;
    static final long FPS = 15;  // dibuixa FPS vegades per segon

    Refresc(MainGame view)
    {
        this.view = view;
    }

    void setRunning(boolean run)
    {
        running = run;
    }

    public void resetView(MainGame view)
    {
        this.view = view;
    }

    @Override
    public void run()
    {
        long i = 0;
        long ticksPS = 1000 / FPS;
        long startTime;
        long sleepTime;
        long elapsedTime;
        while (true)
        {
            Canvas c = null;
            if (running)
            {
                startTime = System.currentTimeMillis();
                try {
                    c = view.getHolder().lockCanvas();
                    synchronized (view.getHolder())
                    {
                        view.paintScreen(c);
                    }
                }

                finally {
                    if (c != null) {
                        view.getHolder().unlockCanvasAndPost(c);
                    }
                }
                elapsedTime = System.currentTimeMillis() - startTime;
                sleepTime = ticksPS - elapsedTime;

                try {
                    if (sleepTime > 0) {
                        sleep(sleepTime);
                    }
                } catch (Exception e) {
                }


            }
        }
    }
}