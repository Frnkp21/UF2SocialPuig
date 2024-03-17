package com.example.socialpuig;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class profileFragment extends Fragment {

    ImageView photoImageView;
    TextView displayNameTextView, emailTextView;
    EditText nuevoNombreEditText;
    Button saveChangesButton;
    ImageButton changePhotoButton;
    ActivityResultLauncher<Intent> galleryLauncher;

    public profileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoImageView = view.findViewById(R.id.photoImageView);
        displayNameTextView = view.findViewById(R.id.nameProfile);
        emailTextView = view.findViewById(R.id.emailProfile);
        nuevoNombreEditText = view.findViewById(R.id.newNameProfile);
        saveChangesButton = view.findViewById(R.id.saveButton);
        changePhotoButton = view.findViewById(R.id.changeButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            displayNameTextView.setText(user.getDisplayName());
            emailTextView.setText(user.getEmail());

            Glide.with(requireView()).load(user.getPhotoUrl()).into(photoImageView);
        }

        // Registro del launcher para abrir la galerÃ­a
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                Uri selectedImageUri = result.getData().getData();
                cambiarFotoPerfil(selectedImageUri);
            }
        });

        changePhotoButton.setOnClickListener(v -> abrirGaleria());

        saveChangesButton.setOnClickListener(v -> {
            String nuevoNombre = nuevoNombreEditText.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                guardarCambios(nuevoNombre);
            } else {
                Toast.makeText(getContext(), "Introduce un nuevo nombre", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void cambiarFotoPerfil(Uri selectedImageUri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(selectedImageUri)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnSuccessListener(aVoid -> {
                        Glide.with(requireView()).load(selectedImageUri).into(photoImageView);
                        Toast.makeText(getContext(), "Foto de perfil actualizada correctamente", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Error al actualizar la foto de perfil", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void guardarCambios(String nuevoNombre) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(nuevoNombre)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            displayNameTextView.setText(nuevoNombre);
                            Toast.makeText(getContext(), "Nombre actualizado correctamente", Toast.LENGTH_SHORT).show();

                            // Actualizar el nombre en Firebase Auth
                            FirebaseUser updatedUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (updatedUser != null) {
                                displayNameTextView.setText(updatedUser.getDisplayName());
                            }
                        } else {
                            Toast.makeText(getContext(), "Error al actualizar el nombre", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}