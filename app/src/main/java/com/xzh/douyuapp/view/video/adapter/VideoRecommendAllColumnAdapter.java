package com.xzh.douyuapp.view.video.adapter;


import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.facebook.drawee.view.SimpleDraweeView;
import com.xzh.baserecyclerviewadapter.BaseViewHolder;
import com.xzh.douyuapp.R;
import com.xzh.douyuapp.model.logic.video.bean.VideoRecommendHotCate;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *  全部栏目
 *
 */
public class VideoRecommendAllColumnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<VideoRecommendHotCate.VideoListBean> mVideoListEntity;
    private  Context context;

    public VideoRecommendAllColumnAdapter(Context context, List<VideoRecommendHotCate.VideoListBean> mVideoListEntity)
    {
          this.context=context;
          this.mVideoListEntity=mVideoListEntity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HotColumnHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_recommend_view,parent,false));
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(holder instanceof HotColumnHolder)
            {
                bindHotColumun((HotColumnHolder) holder,position);
            }
    }
    private void bindHotColumun(HotColumnHolder holder, int position) {
        holder.img_item_gridview.setImageURI(Uri.parse(mVideoListEntity.get(position).getVideo_cover()));
        holder.tv_column_item_nickname.setText(mVideoListEntity.get(position).getVideo_title());
        holder.tv_nickname.setText(mVideoListEntity.get(position).getNickname());

        String str=String.valueOf(mVideoListEntity.get(position).getCtime());
        SimpleDateFormat mSimpeDateFormat=new SimpleDateFormat("hh:mm:ss");
        Date date=new Date( mVideoListEntity.get(position).getCtime());
        holder.tv_video_time.setText(mSimpeDateFormat.format(date));
        holder.tv_watchnum.setText(Integer.toString(mVideoListEntity.get(position).getView_num()));
    }
    @Override
    public int getItemCount() {
        return mVideoListEntity.size();
    }
    public class HotColumnHolder extends BaseViewHolder
    {
        //        图片
        public  SimpleDraweeView  img_item_gridview;
        //        房间名称
        public TextView tv_column_item_nickname;
        //        在线人数
        public TextView tv_video_time;
        //        昵称
        public TextView tv_nickname;

        public TextView tv_watchnum;

        public HotColumnHolder(View view) {
            super(view);
            img_item_gridview=(SimpleDraweeView)view.findViewById(R.id.img_item_gridview);
            tv_column_item_nickname=(TextView)view.findViewById(R.id.tv_column_item_nickname);
            tv_video_time=(TextView)view.findViewById(R.id.tv_video_time);
            tv_nickname=(TextView)view.findViewById(R.id.tv_nickname);
            tv_watchnum=(TextView)view.findViewById(R.id.tv_watchnum);
        }

    }
}
