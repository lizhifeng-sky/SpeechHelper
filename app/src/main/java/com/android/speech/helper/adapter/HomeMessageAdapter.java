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
public class HomeMessageAdapter extends RecyclerView.Adapter<HomeMessageAdapter.HomeMessageHolder> {
    private List<MessageBean> messageBeanList;

    public HomeMessageAdapter(List<MessageBean> messageBeanList) {
        this.messageBeanList = messageBeanList;
    }

    @NonNull
    @Override
    public HomeMessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_message, parent, false);
        return new HomeMessageHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeMessageHolder holder, int position) {
        MessageBean messageBean = messageBeanList.get(position);
        holder.message.setText(messageBean.getMessage());
    }

    @Override
    public int getItemCount() {
        return messageBeanList == null ? 0 : messageBeanList.size();
    }

    static class HomeMessageHolder extends RecyclerView.ViewHolder {
        TextView message;

        public HomeMessageHolder(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
        }
    }

}
