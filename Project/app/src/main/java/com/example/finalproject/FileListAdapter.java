/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.finalproject;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

/***
 * The adapter class for the RecyclerView, contains the sports data.
 */
class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder>  {

    // Member variables.
    private ArrayList<FilenameCard> filenameCards;
    private Context mContext;

    /**
     * Constructor that passes in the sports data and the context.
     *
     * @param sportsData ArrayList containing the sports data.
     * @param context Context of the application.
     */
    FileListAdapter(Context context, ArrayList<FilenameCard> sportsData) {
        this.filenameCards = sportsData;
        this.mContext = context;

    }


    /**
     * Required method for creating the viewholder objects.
     *
     * @param parent The ViewGroup into which the new View will be added
     *               after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return The newly created ViewHolder.
     */
    @Override
    public FileListAdapter.ViewHolder onCreateViewHolder(
            ViewGroup parent, int viewType) {
        View v=LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);
        return new ViewHolder(v);
    }

    /**
     * Required method that binds the data to the viewholder.
     *
     * @param holder The viewholder into which the data should be put.
     * @param position The adapter position.
     */
    @Override
    public void onBindViewHolder(FileListAdapter.ViewHolder holder,
                                 int position) {
        // Get current sport.
        FilenameCard currentSport = filenameCards.get(position);

        // Populate the textviews with data.
        holder.bindTo(currentSport);
    }

    /**
     * Required method for determining the size of the data set.
     *
     * @return Size of the data set.
     */
    @Override
    public int getItemCount() {
        return filenameCards.size();
    }


    /**
     * ViewHolder class that represents each row of data in the RecyclerView.
     */
    class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        // Member Variables for the TextViews
        private TextView filename;
        private TextView Filepath;
        /**
         * Constructor for the ViewHolder, used in onCreateViewHolder().
         *
         * @param itemView The rootview of the list_item.xml layout file.
         */
        ViewHolder(View itemView) {
            super(itemView);

            // Initialize the views.
            //mTitleText = itemView.findViewById(R.id.title);

            filename = itemView.findViewById(R.id.fileNameText);
            Filepath = itemView.findViewById(R.id.Filepath);
            // Set the OnClickListener to the entire view.
            itemView.setOnClickListener(this);
        }

        void bindTo(FilenameCard currentSport){
            // Populate the textviews with data.
            filename.setText(currentSport.getFilename());
            Filepath.setText(currentSport.getFilePath());
            //mInfoText.setText(currentSport.getInfo());
            // Load the images into the ImageView using the Glide library.
            //Glide.with(mContext).load(currentSport.getImageResource()).into(mSportsImage);
        }

        /**
         * Handle click to show DetailActivity.
         *
         * @param view View that is clicked.
         */
        @Override
        public void onClick(View view) {

            FilenameCard currentFile = filenameCards.get(getAdapterPosition());
            Intent detailIntent = new Intent(mContext, RouteActivity.class);
            detailIntent.putExtra("filePath",currentFile.getFilePath());
            mContext.startActivity(detailIntent);
            /*
            Sport currentSport = mSportsData.get(getAdapterPosition());
            Intent detailIntent = new Intent(mContext, DetailActivity.class);
            detailIntent.putExtra("title", currentSport.getTitle());
            detailIntent.putExtra("image_resource",
                    currentSport.getImageResource());
            mContext.startActivity(detailIntent);
            */

        }
    }
}
