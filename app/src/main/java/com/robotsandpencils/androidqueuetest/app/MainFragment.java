package com.robotsandpencils.androidqueuetest.app;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.path.android.jobqueue.AsyncAddCallback;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.JobManager;
import com.path.android.jobqueue.Params;
import com.path.android.jobqueue.config.Configuration;
import com.path.android.jobqueue.log.CustomLogger;

import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    @InjectView(R.id.textView)
    TextView mTextView;
    @InjectView(R.id.button)
    Button mButton;
    JobManager mJobManager;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        Configuration configuration = new Configuration.Builder(getActivity())
                .customLogger(new CustomLogger() {
                    private static final String TAG = "JOBS";
                    @Override
                    public boolean isDebugEnabled() {
                        return true;
                    }

                    @Override
                    public void d(String text, Object... args) {
                        Log.d(TAG, String.format(text, args));
                    }

                    @Override
                    public void e(Throwable t, String text, Object... args) {
                        Log.e(TAG, String.format(text, args), t);
                    }

                    @Override
                    public void e(String text, Object... args) {
                        Log.e(TAG, String.format(text, args));
                    }
                })
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(3)//up to 3 consumers at a time
                .loadFactor(3)//3 jobs per consumer
                .consumerKeepAlive(120)//wait 2 minute
                .build();
        mJobManager = new JobManager(getActivity(), configuration);

        mJobManager.start();

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextView.setText("Ready");
                mJobManager.addJobInBackground(new PostJob("Go Do it!"), new AsyncAddCallback() {
                    @Override
                    public void onAdded(long jobId) {
                        mTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                mTextView.setText("Added");
                            }
                        });
                    }
                });
                mJobManager.start();
            }
        });

        return rootView;
    }

    public static class PostJob extends Job {

        private Random mRandom = new Random();
        private String text;

        public PostJob() {
            super(new Params(1).requireNetwork().persist());
        }

        public PostJob(String text) {
            this();
            this.text = text;
        }

        @Override
        public void onAdded() {
            Log.e("NAS", "onAdded");
        }

        @Override
        public void onRun() throws Throwable {

            Log.e("NAS", "onRun");
            if (mRandom.nextDouble() < 0.98) {
                Log.e("NAS", "Not high enough.");
                throw new Exception("Not high enough.");
            }
            Log.e("NAS", "Sending text... "+ text);
        }

        @Override
        protected void onCancel() {
            Log.e("NAS", "onCancel");
        }

        @Override
        protected boolean shouldReRunOnThrowable(Throwable throwable) {
            return true;
        }

        @Override
        protected int getRetryLimit() {
            return Integer.MAX_VALUE;
        }
    }
}
