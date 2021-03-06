package tech.app4.scadasimulator;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.content.Context;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.content.res.Resources.Theme;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(new MyAdapter(
                toolbar.getContext(),
                new String[]{
                        "Habitación",
                        "Sala",
                        "Otro",
                }));

        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // When the given dropdown item is selected, show its contents in the
                // container view.

                String environment;
                switch (position){
                    case 0: environment = "Habitacion";
                        break;
                    case 1: environment = "Sala";
                        break;
                    default: environment = "Habitacion";
                        break;
                }


                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, PlaceholderFragment.newInstance(position + 1, environment))
                        .commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private static class MyAdapter extends ArrayAdapter<String> implements ThemedSpinnerAdapter {
        private final ThemedSpinnerAdapter.Helper mDropDownHelper;

        public MyAdapter(Context context, String[] objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
            mDropDownHelper = new ThemedSpinnerAdapter.Helper(context);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                // Inflate the drop down using the helper's LayoutInflater
                LayoutInflater inflater = mDropDownHelper.getDropDownViewInflater();
                view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            return view;
        }

        @Override
        public Theme getDropDownViewTheme() {
            return mDropDownHelper.getDropDownViewTheme();
        }

        @Override
        public void setDropDownViewTheme(Theme theme) {
            mDropDownHelper.setDropDownViewTheme(theme);
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef;

        public String mEnvironmentName = "Habitacion";


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, String environmentName) {
            PlaceholderFragment fragment = new PlaceholderFragment();

            fragment.mEnvironmentName = environmentName;

            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final TextView temperatureView = rootView.findViewById(R.id.temperature_label);
            final EditText temperatureLimit = rootView.findViewById(R.id.temperature_limit_edit);
            final SeekBar lightRegulator = (SeekBar) rootView.findViewById(R.id.light_regulator);
            final Switch blowOnOff = (Switch) rootView.findViewById(R.id.blowhole_on_off);

            dbRef = database.getReference(mEnvironmentName);

            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    tech.app4.scadasimulator.model.Environment obj = dataSnapshot.getValue(tech.app4.scadasimulator.model.Environment.class);
                    temperatureView.setText(obj.Temperatura.toString() + "º");
                    temperatureLimit.setText(obj.TemperaturaLimite.toString());
                    temperatureLimit.setSelection(temperatureLimit.getText().length());
                    lightRegulator.setProgress(obj.Luz.intValue());
                    blowOnOff.setChecked(obj.Ventilador.intValue() != 0);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            temperatureLimit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangeTemperatureLimitDialog(temperatureLimit.getText().toString());
                }
            });

            lightRegulator.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    dbRef.child("Luz").setValue((long)seekBar.getProgress());
                }
            });


            blowOnOff.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    final Switch btn = (Switch) v;
                    final boolean switchChecked = btn.isChecked();

                    if (btn.isChecked()) {
                        btn.setChecked(false);
                    } else {
                        btn.setChecked(true);
                    }

                    String message = "Esta seguro de apagar el Ventilador?";
                    if (!btn.isChecked()) {
                        message = "Esta seguro de encender el Ventilador?";
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // Change "this" to `getActivity()` if you're using this on a fragment
                    builder.setTitle("Ventilador");
                    builder.setMessage(message)
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    /*if (switchChecked) {
                                        btn.setChecked(true);
                                    } else {
                                        btn.setChecked(false);
                                    }*/

                                    dbRef.child("Ventilador").setValue( switchChecked ? (long)150 : (long)0 );
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            });


            return rootView;
        }

        public void showChangeTemperatureLimitDialog(String currentValue) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.custom_layout, null);
            dialogBuilder.setView(dialogView);

            final EditText temperatureLimitChanger = (EditText) dialogView.findViewById(R.id.temperature_editText);
            temperatureLimitChanger.setText(currentValue);
            temperatureLimitChanger.setSelection(temperatureLimitChanger.getText().length());

            dialogBuilder.setTitle("Temperatura Limite");
            dialogBuilder.setMessage("Introduzca el nuevo limite: ");
            dialogBuilder.setPositiveButton("Cambiar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = temperatureLimitChanger.getText().toString();
                    if(value.length()>0)
                        dbRef.child("TemperaturaLimite").setValue(Double.parseDouble(value));
                    else
                        Toast.makeText(getActivity(),"No se permiten valores vacios.", Toast.LENGTH_LONG).show();
                }
            });
            dialogBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    //pass
                }
            });
            AlertDialog b = dialogBuilder.create();
            b.show();
        }

    }
}
