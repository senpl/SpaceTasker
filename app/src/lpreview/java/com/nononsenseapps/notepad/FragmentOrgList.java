/*
 * Copyright (c) 2014 Jonas Kalderstam.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nononsenseapps.notepad;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.helpers.Log;
import com.nononsenseapps.notepad.views.FloatingAddButtonFrameLayout;

import org.cowboyprogrammer.org.OrgFile;
import org.cowboyprogrammer.org.OrgNode;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * A {@link Fragment} that displays the contents of an OrgFile.
 */
public class FragmentOrgList extends Fragment {

    private RecyclerView mRecyclerView;
    private OrgFileAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingAddButtonFrameLayout mFloatingAddButton;
    private String[] myDataset = new String[]{"London",
            "Tokyo",
            "Moscow",
            "Budapest",
            "Paris",
            "Kyoto"};
    private OrgFile orgFile;


    public FragmentOrgList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_org_list,
                container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(android.R.id.list);

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // I want some dividers
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));

        // specify an adapter
        mAdapter = new OrgFileAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // FAB
        mFloatingAddButton =
                (FloatingAddButtonFrameLayout) rootView.findViewById(R.id
                        .add_button);
        mFloatingAddButton.setVisibility(View.VISIBLE);
        mFloatingAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // TODO
                // Add a dummy item for now
                OrgNode node = new OrgNode();
                node.setTitle("Dummy new item");
                node.setBody("Dummy body");
                mAdapter.addNodeToEnd(node);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Load a dummy file
        try {
            orgFile = OrgFile.createFromString("DummyFile",
                    "* First header :tag1:tag2:\n" +
                    "With some text in the body\n\n" +
                    "** Second header :tag1:tag2:\n" +
                    "With more text in its body\n\n" +
                    "* Third header :tag1:tag2:\n" +
                    "With even more text in body.\n");
            mAdapter.setData(orgFile);
        } catch (ParseException e) {
            // TODO
            e.printStackTrace();
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    class OrgFileAdapter extends RecyclerView.Adapter<OrgFileAdapter
            .ViewHolder> {

        OrgFile data = null;
        ArrayList<OrgNode> items = null;

        /**
         * Get the data backing the adapter
         * @return OrgFile
         */
        public OrgFile getData() {
            return data;
        }

        /**
         * Update the data in the adapter
         * @param data OrgFile
         */
        public void setData(OrgFile data) {
            this.data = data;
            if (data == null)
                items = null;
            else
                items = getDescendants(data);

            // Tell the UI to update the list
            this.notifyDataSetChanged();
        }

        /**
         * Adds all subnodes and their descendants a list and returns it.
         * @param orgFile
         * @return
         */
        ArrayList<OrgNode> getDescendants(OrgFile orgFile) {
            ArrayList<OrgNode> list = new ArrayList<OrgNode>();
            for (OrgNode child: orgFile.getSubNodes()) {
                list.add(child);
                addDescendants(child, list);
            }

            return list;
        }

        /**
         * Add descendants of node to the list. Calls itself recursively.
         * @param node
         * @param list
         */
        void addDescendants(OrgNode node, ArrayList<OrgNode> list) {
            for (OrgNode child: node.getSubNodes()) {
                list.add(child);
                addDescendants(child, list);
            }
        }

        /**
         * Adds an OrgNode to the end of the list
         * @param node to add
         */
        void addNodeToEnd(OrgNode node) {
            node.setLevel(data.getLevel() + 1);
            node.setParent(data);
            data.getSubNodes().add(node);
            items.add(node);
            notifyItemInserted(items.size() -1);
        }

        // Provide a reference to the type of views that you are using
        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public TextView mTextView;
            public ViewHolder(View v) {
                super(v);
                v.setOnClickListener(this);

                mTextView = (TextView) v.findViewById(android.R.id.text1);
            }

            /**
             * OnItemClickListener replacement
             * @param view
             */
            @Override
            public void onClick(final View view) {
                Toast.makeText(view.getContext(), "Clicked " + getPosition()
                                                  + ": " + items.get
                        (getPosition()).getTitle(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public OrgFileAdapter.ViewHolder onCreateViewHolder(
                final ViewGroup parent, final int i) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.file_list_item, parent, false);
            // set the view's size, margins, paddings and layout parameters
            //...
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(final OrgFileAdapter.ViewHolder holder,
                final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mTextView.setText(items.get(position).getTitle());
        }

        @Override
        public int getItemCount() {
            if (items == null)
                return 0;
            else
                return items.size();
        }
    }
}
