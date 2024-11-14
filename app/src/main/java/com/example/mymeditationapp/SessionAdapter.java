package com.example.mymeditationapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Date;
import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.ViewHolder> {
    private List<MeditationSession> sessions;
    private static final String TAG = "SessionAdapter";

    public SessionAdapter(List<MeditationSession> sessions) {
        this.sessions = sessions;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSessionType;
        public TextView tvTime;
        public RelativeLayout rlBorder;

        public ViewHolder(View itemView) {
            super(itemView);
            tvSessionType = itemView.findViewById(R.id.tvSessionType);
            tvTime = itemView.findViewById(R.id.tvTime);
            rlBorder = itemView.findViewById(R.id.rlBorder);
        }

        public void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MeditationSession session = sessions.get(position);
        Log.d("color", session.getType());
        //changes the colour of the recycle view element based on the status of the session
        switch(session.getStatus()){
            case "complete":
                holder.rlBorder.setBackgroundResource(R.drawable.border_green);
                break;
            case "incomplete":
                holder.rlBorder.setBackgroundResource(R.drawable.border_red);
                break;
            case "awaiting":
                holder.rlBorder.setBackgroundResource(R.drawable.border_blue);
                break;

        }
        holder.tvSessionType.setText(session.getType());
        holder.tvTime.setText(session.getTimeFromDate());
        //Action listener for if user clicks on one of the sessions
        holder.setOnClickListener(v -> {
            boolean isSelecting = ((ViewMeditationSessionsActivity) v.getContext()).isSelecting();
            MeditationSession clickedSession = sessions.get(holder.getAdapterPosition());
            Date date10MinsBefore = new Date(new Date().getTime() - (10 * 60 * 1000));
            Date date10MinsAfter = new Date(new Date().getTime() + (10 * 60 * 1000));
            if(isSelecting){ //if delete state is enabled
                if(clickedSession.getStatus().equals("awaiting") && clickedSession.getDueDate().after(date10MinsBefore)){
                    v.getContext().getContentResolver().delete(
                            MeditationContract.MeditationSessionTable.CONTENT_URI,
                            "sessionId=?",
                            new String[]{Integer.toString(session.getSessionId())}); //delete query
                    sessions.remove(holder.getAdapterPosition());
                    ((ViewMeditationSessionsActivity) v.getContext()).loadRvData(clickedSession.getDueDate());
                }else{
                    Toast.makeText(v.getContext(), "Cannot delete a\nnon-awaiting session", Toast.LENGTH_SHORT).show();
                }
            }else{
                if(clickedSession.getStatus().equals("awaiting") && clickedSession.getDueDate().after(date10MinsBefore) &&
                        clickedSession.getDueDate().before(date10MinsAfter)){ //if status is awaiting and the sessions is after 10 mins ago and before 10 mins after now
                    Class nextClass = null; //Move them to the correct intent to start the session
                    if(clickedSession.getType().equals("Mindfulness")){
                        nextClass = MindfulnessFormActivity.class;
                    }else if(clickedSession.getType().equals("Breathing Exercise")){
                        nextClass = BreathingFormActivity.class;
                    }
                    Intent intent = new Intent(v.getContext(),nextClass);
                    intent.putExtra("id",clickedSession.getSessionId());
                    intent.putExtra("duration",clickedSession.getDuration()); //add the required data
                    v.getContext().startActivity(intent);
                }else{
                    if(clickedSession.getDueDate().after(date10MinsAfter)){
                        Toast.makeText(v.getContext(), "Session hasn't started", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(v.getContext(), "Session has expired", Toast.LENGTH_SHORT).show();
                        Log.d(TAG,""+(new Date(new Date().getTime() + 10 * 60 * 1000)));
                        Log.d(TAG,""+(clickedSession.getDueDate()));

                    }
                }
            }

        });
    }


    @Override
    public int getItemCount() {
        return sessions.size();
    }
}
