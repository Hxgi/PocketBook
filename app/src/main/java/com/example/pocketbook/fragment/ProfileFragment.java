package com.example.pocketbook.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.pocketbook.model.BookList;
import com.example.pocketbook.model.User;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pocketbook.GlideApp;
import com.example.pocketbook.R;
import com.example.pocketbook.adapter.BookAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ProfileFragment extends Fragment {
    private static final int numColumns = 2;
    private static final int LIMIT = 20;
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private RecyclerView mBooksRecycler;
    private BookAdapter mAdapter;
    private TextView profileName, userName;
    private TextView editProfile;
    private static final String USERS = "users";
    private User currentUser;

    public ProfileFragment(){
        // Empty Constructor
    }

    public ProfileFragment(User currentUser){
        this.currentUser = currentUser;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (container != null) {
          container.removeAllViews();
        }
        View v = inflater.inflate(R.layout.profile_layout, container, false);
        String first_Name = currentUser.getFirstName();
        String last_Name = currentUser.getLastName();
        String user_Name = currentUser.getUsername();
        // TODO: obtain user_photo from firebase
        String user_Pic = currentUser.getPhoto();

        TextView ProfileName = (TextView) v.findViewById(R.id.profileName);
        TextView UserName = (TextView) v.findViewById(R.id.user_name);
        ProfileName.setText(first_Name + ' ' + last_Name);
        UserName.setText(user_Name);

        editProfile = v.findViewById(R.id.edit_profile_button);
        mBooksRecycler = v.findViewById(R.id.recycler_books);
        mAdapter = new BookAdapter(mQuery);
        mBooksRecycler.setLayoutManager(new GridLayoutManager(v.getContext(), numColumns));
        mBooksRecycler.setAdapter(mAdapter);

//        private FirebaseAuth mAuth;
//        FirebaseStorage storage = FirebaseStorage.getInstance();
//        StorageReference storageRef = storage.getReferenceFromUrl("gs://am-d5edb.appspot.com").child("users").child(mAuth.getUid()+".jpg");
//
//        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                Log.e("Tuts+", "uri: " + uri.toString());
//                DownloadLink = uri.toString();
//                CircleImageView iv = (CircleImageView) view.findViewById(R.id.profilePictureEditFragment);
//                Picasso.with(getContext()).load(uri.toString()).placeholder(R.drawable.ic_launcher3slanted).error(R.drawable.ic_launcher3slanted).into(iv);
//                //Handle whatever you're going to do with the URL here
//            }
//        });



        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditProfileFragment nextFrag = new EditProfileFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container,nextFrag).commit();
            }
        });
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String firstName = currentUser.getFirstName();
        // Initialize Firestore and main RecyclerView
        initFirestore();
//        generateData();
//        initRecyclerView();
    }

    private void initFirestore(){
        mFirestore = FirebaseFirestore.getInstance();
        // Query to retrieve all books
        mQuery = mFirestore.collection("books")
                .limit(LIMIT);
    }


    private void retrieveData() {
        // for demonstration purposes
        // query to retrieve all books
        Query query = mFirestore.collection("books");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }
}