package com.android.speech.helper.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.speech.helper.R;
import com.android.speech.helper.bean.MessageBean;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author lizhifeng
 * @date 2020/12/24 11:23
 */
public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicHolder> {
    private List<String> musicList;
    private OnItemClickListener onItemClickListener;

    public MusicAdapter(List<String> musicList,OnItemClickListener onItemClickListener) {
        this.musicList = musicList;
        this.onItemClickListener=onItemClickListener;
    }

    @NonNull
    @Override
    public MusicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_message, parent, false);
        return new MusicHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicHolder holder, int position) {
        String music = musicList.get(position);
        holder.message.setText(music);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener!=null){
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList == null ? 0 : musicList.size();
    }

    static class MusicHolder extends RecyclerView.ViewHolder {
        TextView message;

        public MusicHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

}
