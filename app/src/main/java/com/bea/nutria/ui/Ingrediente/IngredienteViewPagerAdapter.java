package com.bea.nutria.ui.Ingrediente;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class IngredienteViewPagerAdapter extends FragmentStateAdapter {

    public IngredienteViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new IngredientesRegistradosFragment();
        } else {
            return new NovoIngredienteFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}