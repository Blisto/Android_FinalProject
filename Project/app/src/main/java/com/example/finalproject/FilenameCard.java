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

/**
 * Data model for each row of the RecyclerView
 */
class FilenameCard {

    // Member variables representing the title and information about the sport.
    private String Fname;
    private String FilePath;

    public FilenameCard(String Fname,String FilePath) {
        this.Fname = Fname;
        this.FilePath = FilePath;

    }
    String getFilename() {
        return Fname;
    }
    String getFilePath() {
        return FilePath;
    }
}
