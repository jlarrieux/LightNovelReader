package com.jeannius.lightnovelreader.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jeannius.lightnovelreader.R;
import com.jeannius.lightnovelreader.model.Novel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NovelAdapter extends RecyclerView.Adapter<NovelAdapter.NovelViewHolder> {
    
    private List<Novel> novels;
    private Context context;
    private OnNovelClickListener clickListener;
    private OnNovelLongClickListener longClickListener;
    private SimpleDateFormat dateFormat;
    
    public interface OnNovelClickListener {
        void onNovelClick(Novel novel);
    }
    
    public interface OnNovelLongClickListener {
        void onNovelLongClick(Novel novel, View view);
    }
    
    public NovelAdapter(Context context, List<Novel> novels) {
        this.context = context;
        this.novels = novels;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }
    
    public void setOnNovelClickListener(OnNovelClickListener listener) {
        this.clickListener = listener;
    }
    
    public void setOnNovelLongClickListener(OnNovelLongClickListener listener) {
        this.longClickListener = listener;
    }
    
    @NonNull
    @Override
    public NovelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_novel, parent, false);
        return new NovelViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NovelViewHolder holder, int position) {
        Novel novel = novels.get(position);
        
        holder.titleTextView.setText(novel.getTitle());
        
        // Show chapter info
        if (novel.getCurrentChapter() != null && !novel.getCurrentChapter().isEmpty()) {
            holder.chapterTextView.setText(novel.getCurrentChapter());
            holder.chapterTextView.setVisibility(View.VISIBLE);
        } else {
            holder.chapterTextView.setVisibility(View.GONE);
        }
        
        // Show last read date
        String lastRead = getLastReadText(novel.getLastReadDate());
        holder.lastReadTextView.setText("Last read: " + lastRead);
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNovelClick(novel);
            }
        });
        
        // Set long click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onNovelLongClick(novel, v);
            }
            return true;
        });
    }
    
    @Override
    public int getItemCount() {
        return novels.size();
    }
    
    public void updateNovels(List<Novel> newNovels) {
        this.novels = newNovels;
        notifyDataSetChanged();
    }
    
    private String getLastReadText(long lastReadDate) {
        long now = System.currentTimeMillis();
        long diff = now - lastReadDate;
        
        // Less than a minute
        if (diff < 60 * 1000) {
            return "Just now";
        }
        // Less than an hour
        else if (diff < 60 * 60 * 1000) {
            long minutes = diff / (60 * 1000);
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }
        // Less than a day
        else if (diff < 24 * 60 * 60 * 1000) {
            long hours = diff / (60 * 60 * 1000);
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }
        // Less than a week
        else if (diff < 7 * 24 * 60 * 60 * 1000) {
            long days = diff / (24 * 60 * 60 * 1000);
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }
        // Otherwise show date
        else {
            return dateFormat.format(new Date(lastReadDate));
        }
    }
    
    static class NovelViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView chapterTextView;
        TextView lastReadTextView;
        
        public NovelViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.novel_title);
            chapterTextView = itemView.findViewById(R.id.novel_chapter);
            lastReadTextView = itemView.findViewById(R.id.novel_last_read);
        }
    }
}