package com.example.pocketbook.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.pocketbook.GlideApp;
import com.example.pocketbook.R;
import com.example.pocketbook.model.Book;
import com.example.pocketbook.model.User;
import com.example.pocketbook.util.FirebaseIntegrity;
import com.example.pocketbook.util.Parser;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Allows the user to add a new book to be placed/available on PocketBook
 */
public class AddBookActivity extends AppCompatActivity {

//    Book book;
    User currentUser;

    private Boolean validTitle;
    private Boolean validAuthor;
    private Boolean validISBN;
    private int LAUNCH_CAMERA_CODE = 1408;
    private int LAUNCH_GALLERY_CODE = 1922;

    String currentPhotoPath;
    Bitmap galleryPhoto;
    Boolean showRemovePhoto;

    StorageReference defaultBookCover = FirebaseStorage.getInstance().getReference()
            .child("default_images").child("no_book_cover_light.png");

    TextInputEditText layoutBookTitle;
    TextInputEditText layoutBookAuthor;
    TextInputEditText layoutBookISBN;
    ImageView layoutBookCover;
    TextInputEditText layoutBookCondition;
    TextInputEditText layoutBookComment;

    TextInputLayout layoutBookTitleContainer;
    TextInputLayout layoutBookAuthorContainer;
    TextInputLayout layoutBookISBNContainer;
    TextInputLayout layoutBookConditionContainer;
    TextInputLayout layoutBookCommentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("HA_USER");

        // return if the intent passes in a null user
        if (currentUser == null) {
            return;
        }

        showRemovePhoto = false;  // book without a photo can't have photo removed

        // initialize validation booleans to false
        validTitle = false;
        validAuthor = false;
        validISBN = false;

        // Toolbar toolbar = (Toolbar) findViewById(R.id.addBookToolbar);
        ImageView cancelButton = (ImageView) findViewById(R.id.addBookCancelBtn);
        TextView saveButton = (TextView) findViewById(R.id.addBookSaveBtn);
        TextView changePhotoButton = (TextView) findViewById(R.id.addBookChangePhotoBtn);

        // access the layout text fields
        layoutBookTitle = (TextInputEditText) findViewById(R.id.addBookTitleField);
        layoutBookAuthor = (TextInputEditText) findViewById(R.id.addBookAuthorField);
        layoutBookISBN = (TextInputEditText) findViewById(R.id.addBookISBNField);
        layoutBookCover = (ImageView) findViewById(R.id.addBookBookCoverField);
        layoutBookCondition = (TextInputEditText) findViewById(R.id.addBookConditionField);
        layoutBookComment = (TextInputEditText) findViewById(R.id.addBookCommentField);

        // set the initial book condition
        layoutBookCondition.setText(R.string.fairCondition);

        // access the layout text containers
        layoutBookTitleContainer = (TextInputLayout) findViewById(R.id.addBookTitleContainer);
        layoutBookAuthorContainer = (TextInputLayout) findViewById(R.id.addBookAuthorContainer);
        layoutBookISBNContainer = (TextInputLayout) findViewById(R.id.addBookISBNContainer);
        layoutBookConditionContainer = (TextInputLayout)
                findViewById(R.id.addBookConditionContainer);
        layoutBookCommentContainer = (TextInputLayout) findViewById(R.id.addBookCommentContainer);

        // add a text field listener that validates the inputted text
        layoutBookTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // if the inputted text is invalid
                if (!(Parser.isValidBookTitle(s.toString()))) {
                    layoutBookTitle.setError("Input required");
                    layoutBookTitleContainer.setErrorEnabled(true);
                } else {  // if the inputted text is valid
                    validTitle = true;
                    layoutBookTitle.setError(null);
                    layoutBookTitleContainer.setErrorEnabled(false);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // add a text field listener that validates the inputted text
        layoutBookAuthor.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // if the inputted text is invalid
                if (!(Parser.isValidBookAuthor(s.toString()))) {
                    layoutBookAuthor.setError("Input required");
                    layoutBookAuthorContainer.setErrorEnabled(true);
                } else {  // if the inputted text is valid
                    validAuthor = true;
                    layoutBookAuthor.setError(null);
                    layoutBookAuthorContainer.setErrorEnabled(false);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // add a text field listener that validates the inputted text
        layoutBookISBN.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // if the inputted text is invalid
                if (!(Parser.isValidBookIsbn(s.toString()))) {
                    // if the inputted text is empty
                    if (s.toString().equals("")) {
                        layoutBookISBN.setError("Input required");
                    } else {  // if the inputted text is otherwise invalid
                        layoutBookISBN.setError("Invalid ISBN");
                    }
                    layoutBookISBNContainer.setErrorEnabled(true);
                } else {  // if the inputted text is valid
                    validISBN = true;
                    layoutBookISBN.setError(null);
                    layoutBookISBNContainer.setErrorEnabled(false);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // showSpinnerDialog when layoutBookCondition is clicked
        layoutBookCondition.setOnClickListener(v -> showSpinnerDialog());

        // showImageSelectorDialog when changePhotoButton is clicked
        changePhotoButton.setOnClickListener(v -> showImageSelectorDialog());

        // load default book cover into ImageLayout
        GlideApp.with(Objects.requireNonNull(getApplicationContext()))
                .load(defaultBookCover)
                .into(layoutBookCover);

        // go back when cancelButton is clicked
        cancelButton.setOnClickListener(v -> onBackPressed());

        // when saveButton is clicked
        saveButton.setOnClickListener(v -> {
            // if all fields are valid
            if (validTitle && validAuthor && validISBN) {
                if (!noChanges()) {  // if the user has entered some text or chosen a photo

                    // extract the layout text field values into variables
                    String title = Objects.requireNonNull(layoutBookTitle.getText())
                            .toString();
                    String author = Objects.requireNonNull(layoutBookAuthor.getText())
                            .toString();
                    String isbn = Objects.requireNonNull(layoutBookISBN.getText())
                            .toString();
                    String condition = Objects.requireNonNull(layoutBookCondition.getText())
                            .toString();
                    String comment = Objects.requireNonNull(layoutBookComment.getText())
                            .toString();

                    // generate a valid id for the new book
                    String bookId = Parser.generateValidId();

                    // if all booleans are good, pushNewBook
                    Book book = Parser.parseBook(bookId, title, author, isbn,
                            currentUser.getEmail(), "AVAILABLE", comment,
                            condition, "", new ArrayList<>());

                    if (currentPhotoPath != null) {  // if the user has a photo
                        // set the user's photo appropriately
                        if (currentPhotoPath.equals("BITMAP")) {
                            // if the user chose a photo from the gallery
                            FirebaseIntegrity.pushNewBookToFirebaseWithBitmap(book,
                                    galleryPhoto);
                        } else {  // if the user took a photo
                            FirebaseIntegrity.pushNewBookToFirebaseWithURL(book,
                                    currentPhotoPath);
                        }
                    } else {  // if the user does not have a photo
                        FirebaseIntegrity.pushNewBookToFirebaseWithURL(book, null);
                    }

                    // return from AddBookActivity with a positive result code
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("ABA_BOOK", book);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();

                }
            } else {  // if not all fields are valid
                if (!validTitle) {
                    // set an error and focus the app on the erroneous field
                    layoutBookTitle.setError("Input required");
                    layoutBookTitleContainer.setErrorEnabled(true);
                    layoutBookTitle.requestFocus();
                } else if (!validAuthor) {
                    // set an error and focus the app on the erroneous field
                    layoutBookAuthor.setError("Input required");
                    layoutBookAuthorContainer.setErrorEnabled(true);
                    layoutBookAuthor.requestFocus();
                } else {
                    String isbn = Objects.requireNonNull(layoutBookISBN
                            .getText()).toString();

                    // set an error and focus the app on the erroneous field
                    if (isbn.equals("")) {
                        layoutBookISBN.setError("Input required");
                    } else {
                        layoutBookISBN.setError("Invalid ISBN");
                    }
                    layoutBookISBNContainer.setErrorEnabled(true);
                    layoutBookISBN.requestFocus();
                }
            }
        });
    }

    /**
     * Back button
     */
    @Override
    public void onBackPressed() {
        if (noChanges()) {  // if the user changed nothing
            finish();
        } else {  // if the user has entered some text or chosen a photo
            showCancelDialog();
        }
    }

    /**
     * Spinner Dialog that allows the user to choose the book's condition
     */
    private void showSpinnerDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.alert_dialog_condition_spinner, null);

        // access the spinner text fields
        TextView greatOption = view.findViewById(R.id.spinnerDialogGreatField);
        TextView goodOption = view.findViewById(R.id.spinnerDialogGoodField);
        TextView fairOption = view.findViewById(R.id.spinnerDialogFairField);
        TextView acceptableOption = view.findViewById(R.id.spinnerDialogAcceptableField);
        TextView selectedOption;

        // if the condition layout is not null and has text
        if ((layoutBookCondition != null) && (layoutBookCondition.getText() != null)) {
            // set selectedOption based on the condition layout text
            switch (layoutBookCondition.getText().toString()) {
                case "GREAT":
                    selectedOption = greatOption;
                    break;
                case "GOOD":
                    selectedOption = goodOption;
                    break;
                case "FAIR":
                    selectedOption = fairOption;
                    break;
                default:
                    selectedOption = acceptableOption;
                    break;
            }

            // set the background color of the selected option to red
            selectedOption.setBackgroundColor(ContextCompat
                    .getColor(getBaseContext(), R.color.colorAccent));

            // set the text color of the selected option to white
            selectedOption.setTextColor(ContextCompat
                    .getColor(getBaseContext(), R.color.textWhite));
        }

        // create the condition dialog
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();
        Objects.requireNonNull(alertDialog.getWindow())
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        // set the condition layout text to great when greatOption is clicked
        greatOption.setOnClickListener(v -> {
            alertDialog.dismiss();
            layoutBookCondition.setText(R.string.greatCondition);
        });

        // set the condition layout text to good when goodOption is clicked
        goodOption.setOnClickListener(v -> {
            alertDialog.dismiss();
            layoutBookCondition.setText(R.string.goodCondition);
        });

        // set the condition layout text to fair when fairOption is clicked
        fairOption.setOnClickListener(v -> {
            alertDialog.dismiss();
            layoutBookCondition.setText(R.string.fairCondition);
        });

        // set the condition layout text to acceptable when acceptableOption is clicked
        acceptableOption.setOnClickListener(v -> {
            alertDialog.dismiss();
            layoutBookCondition.setText(R.string.acceptableCondition);
        });
    }

    /**
     * Cancel Dialog that prompts the user to keep editing or to discard their changes
     */
    private void showCancelDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.alert_dialog_discard_changes, null);

        // access the views for the buttons
        Button keepEditingBtn = view.findViewById(R.id.keepEditingBtn);
        TextView discardBtn = view.findViewById(R.id.discardBtn);

        // create the cancel dialog
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();
        Objects.requireNonNull(alertDialog.getWindow())
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        // stay in this activity if the user opts to keep editing
        keepEditingBtn.setOnClickListener(v -> alertDialog.dismiss());

        // finish this activity if the user opts to discard their changes
        discardBtn.setOnClickListener(v -> {
            alertDialog.dismiss();
            SystemClock.sleep(300);
            finish();
        });
    }

    /**
     * Checks if the user has not entered any text or chosen a photo
     * @return true if the user has not changed anything, false otherwise
     */
    private boolean noChanges() {
        return String.valueOf(layoutBookTitle.getText()).equals("")
                && String.valueOf(layoutBookAuthor.getText()).equals("")
                && String.valueOf(layoutBookISBN.getText()).equals("")
                && String.valueOf(layoutBookCondition.getText()).equals("FAIR")
                && String.valueOf(layoutBookComment.getText()).equals("")
                && (currentPhotoPath == null)
                ;
    }


    /**
     * Image Option dialog that allows the user to take, choose, or remove a photo
     */
    private void showImageSelectorDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.alert_dialog_book_photo, null);

        // access the photo option text fields
        TextView takePhotoOption = view.findViewById(R.id.takePhotoField);
        TextView choosePhotoOption = view.findViewById(R.id.choosePhotoField);
        TextView showRemovePhotoOption = view.findViewById(R.id.removePhotoField);

        if (showRemovePhoto) {  // only show the Remove Photo option if the book has a photo
            showRemovePhotoOption.setVisibility(View.VISIBLE);
        } else {
            showRemovePhotoOption.setVisibility(View.GONE);
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();
        Objects.requireNonNull(alertDialog.getWindow())
                .setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        // if the user opts to take a photo, open the camera
        takePhotoOption.setOnClickListener(v -> {
            alertDialog.dismiss();
            openCamera();
        });

        // if the user opts to choose a photo, open their gallery
        choosePhotoOption.setOnClickListener(v -> {
            alertDialog.dismiss();
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(intent, "Select Image"),
                    LAUNCH_GALLERY_CODE);
        });

        // if the user opts to remove their photo, replace their image with the default image
        showRemovePhotoOption.setOnClickListener(v -> {
            alertDialog.dismiss();
            GlideApp.with(Objects.requireNonNull(getApplicationContext()))
                    .load(defaultBookCover)
                    .into(layoutBookCover);
            currentPhotoPath = "REMOVE";
            showRemovePhoto = false;  // don't show Remove Photo option since book has no photo
        });
    }

    /**
     * Allows the camera to be initiated upon request from the user
     */
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // create the file where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // catch errors that occur while creating the file
                Log.e("ADD_BOOK_ACTIVITY", ex.toString());
            }
            // continue only if the file was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                // open the camera
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, LAUNCH_CAMERA_CODE);
            }
        } else {  // if there's no camera activity to handle the intent
            Log.e("ADD_BOOK_ACTIVITY", "Failed to resolve activity!");
        }

    }

    /**
     * Create an image file for the images to be stored
     * @return the created image
     * @throws IOException exception if creating the image file fails
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.CANADA).format(new Date());
        String imageFileName = "JPG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    /**
     * Sets the user's photo to the image from either the camera or the gallery.
     * @param requestCode code that the image activity was launched with
     * @param resultCode code that the image activity returns
     * @param data data from the intent
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // if the user launched the camera
        if (requestCode == LAUNCH_CAMERA_CODE) {
            if(resultCode == Activity.RESULT_OK) {  // if a photo was successfully chosen
                // set the book cover ImageView to the chosen image
                Bitmap myBitmap = BitmapFactory.decodeFile(currentPhotoPath);
                ImageView myImage = (ImageView) findViewById(R.id.addBookBookCoverField);
                myImage.setImageBitmap(myBitmap);
                showRemovePhoto = true;  // show Remove Photo option since user now has a photo
                galleryPhoto = null;  // nullify the gallery photo variable
            } else if (resultCode == Activity.RESULT_CANCELED) {  // if the activity was cancelled
                Log.e("ADD_BOOK_ACTIVITY", "Camera failed!");
            }
        } else if (requestCode == LAUNCH_GALLERY_CODE) {  // if the user launched the gallery
            if(resultCode == Activity.RESULT_OK) {  // if a photo was successfully selected
                try {  // try to get a Bitmap of the selected image
                    InputStream inputStream = getBaseContext()
                            .getContentResolver()
                            .openInputStream(Objects.requireNonNull(data.getData()));
                    // store the selected image in galleryPhoto
                    galleryPhoto = BitmapFactory.decodeStream(inputStream);
                    currentPhotoPath = "BITMAP";
                    ImageView myImage = (ImageView) findViewById(R.id.addBookBookCoverField);
                    myImage.setImageBitmap(galleryPhoto);
                    showRemovePhoto = true;  // show Remove Photo option since user now has a photo

                } catch (FileNotFoundException e) {  // handle when the selected image is not found
                    e.printStackTrace();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {  // if the activity was cancelled
                Log.e("ADD_BOOK_ACTIVITY", "Failed Gallery!");
            }
        }
    }
}
