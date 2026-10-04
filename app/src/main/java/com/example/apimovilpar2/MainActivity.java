package com.example.apimovilpar2;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controlador Principal de la Aplicación.
 * Gestiona la interfaz del inventario, la búsqueda en tiempo real, el listado en RecyclerView
 * y la invocación de diálogos para la creación, modificación y eliminación de productos.
 */
public class MainActivity extends AppCompatActivity implements ProductAdapter.OnProductActionListener {

    // Componentes de la vista
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private FloatingActionButton fabAddProduct;
    private TextInputEditText etSearchQuery;
    private Spinner spinnerCategoryFilter;
    private LinearLayout llEmptyState;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;

    // Ayudante de Base de Datos y Datos
    private DatabaseHelper dbHelper;
    private List<Product> currentProductList;

    // Categorías predefinidas para los productos de tecnología
    private static final String[] CATEGORIES = new String[]{
            "Laptops",
            "Smartphones",
            "Periféricos",
            "Monitores",
            "Audio",
            "Componentes",
            "Accesorios"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Helper de Base de Datos
        dbHelper = new DatabaseHelper(this);
        currentProductList = new ArrayList<>();

        // Inicializar componentes de la UI
        initUI();

        // Configurar RecyclerView y Adaptador
        setupRecyclerView();

        // Configurar Filtros (Búsqueda por texto y Spinner por categoría)
        setupFilters();

        // Configurar Listener del Botón Flotante (FAB)
        fabAddProduct.setOnClickListener(v -> showProductDialog(null));

        // Cargar productos iniciales desde la base de datos
        loadProducts();
    }

    /**
     * Inicializa las referencias de las vistas desde el layout activity_main.xml.
     */
    private void initUI() {
        rvProducts = findViewById(R.id.rvProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);
        etSearchQuery = findViewById(R.id.etSearchQuery);
        spinnerCategoryFilter = findViewById(R.id.spinnerCategoryFilter);
        llEmptyState = findViewById(R.id.llEmptyState);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
    }

    /**
     * Configura el RecyclerView con un LayoutManager lineal y asigna el adaptador personalizado.
     */
    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(this, currentProductList, this);
        rvProducts.setAdapter(productAdapter);
    }

    /**
     * Configura el Spinner de categorías y el TextWatcher del campo de búsqueda para filtrado dinámico.
     */
    private void setupFilters() {
        // Crear lista de categorías para el filtro principal incluyendo "Todas las categorías"
        List<String> filterCategories = new ArrayList<>();
        filterCategories.add(getString(R.string.filter_all));
        filterCategories.addAll(Arrays.asList(CATEGORIES));

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                filterCategories
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoryFilter.setAdapter(spinnerAdapter);

        // Listener para cambio de categoría seleccionada
        spinnerCategoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Sin acción
            }
        });

        // TextWatcher para filtrar mientras el usuario escribe
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadProducts();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Carga o recarga la lista de productos desde SQLite aplicando los filtros de búsqueda activos.
     */
    private void loadProducts() {
        String searchQuery = etSearchQuery.getText() != null ? etSearchQuery.getText().toString().trim() : "";
        String selectedCategory = spinnerCategoryFilter.getSelectedItem() != null ?
                spinnerCategoryFilter.getSelectedItem().toString() : getString(R.string.filter_all);

        // Consultar productos filtrados
        currentProductList = dbHelper.searchProducts(searchQuery, selectedCategory);

        // Actualizar el adaptador
        productAdapter.updateList(currentProductList);

        // Actualizar visibilidad de vista vacía
        updateEmptyState(searchQuery, selectedCategory);
    }

    /**
     * Controla la visibilidad de la pantalla de estado vacío cuando no existen productos.
     */
    private void updateEmptyState(String searchQuery, String selectedCategory) {
        if (currentProductList.isEmpty()) {
            rvProducts.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);

            if (!searchQuery.isEmpty() || !selectedCategory.equals(getString(R.string.filter_all))) {
                tvEmptyTitle.setText(R.string.empty_search_title);
                tvEmptySubtitle.setText(R.string.empty_search_subtitle);
            } else {
                tvEmptyTitle.setText(R.string.empty_products_title);
                tvEmptySubtitle.setText(R.string.empty_products_subtitle);
            }
        } else {
            rvProducts.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
        }
    }

    // =========================================================================
    // LISTENERS DE ACCIONES EN CADA ÍTEM DEL ADAPTADOR
    // =========================================================================

    @Override
    public void onEditProduct(Product product) {
        // Invocación del diálogo para EDITAR producto existente
        showProductDialog(product);
    }

    @Override
    public void onDeleteProduct(Product product) {
        // Invocación del diálogo de confirmación para ELIMINAR producto
        showDeleteConfirmationDialog(product);
    }

    // =========================================================================
    // DIÁLOGOS DE FORMULARIO (CREAR/EDITAR) Y ELIMINACIÓN
    // =========================================================================

    /**
     * Muestra un AlertDialog con formulario para CREAR o EDITAR un producto.
     *
     * @param productToEdit Objeto Producto a editar, o null si se está creando un nuevo producto.
     */
    private void showProductDialog(@Nullable Product productToEdit) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_product, null);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextInputLayout tilName = dialogView.findViewById(R.id.tilName);
        TextInputLayout tilPrice = dialogView.findViewById(R.id.tilPrice);
        TextInputLayout tilStock = dialogView.findViewById(R.id.tilStock);

        TextInputEditText etProductName = dialogView.findViewById(R.id.etProductName);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerDialogCategory);
        TextInputEditText etProductPrice = dialogView.findViewById(R.id.etProductPrice);
        TextInputEditText etProductStock = dialogView.findViewById(R.id.etProductStock);

        // Cargar adaptador para Spinner del diálogo
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                CATEGORIES
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        boolean isEditMode = (productToEdit != null);

        if (isEditMode) {
            tvDialogTitle.setText(R.string.title_edit_product);
            etProductName.setText(productToEdit.getNombre());
            etProductPrice.setText(String.valueOf(productToEdit.getPrecio()));
            etProductStock.setText(String.valueOf(productToEdit.getStock()));

            // Seleccionar categoría actual en el Spinner
            int categoryIndex = Arrays.asList(CATEGORIES).indexOf(productToEdit.getCategoria());
            if (categoryIndex >= 0) {
                spinnerCategory.setSelection(categoryIndex);
            }
        } else {
            tvDialogTitle.setText(R.string.title_add_product);
        }

        // Construir el AlertDialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton(isEditMode ? R.string.action_update : R.string.action_save, null) // Override más abajo para validar antes de cerrar
                .setNegativeButton(R.string.action_cancel, (dialogInterface, which) -> dialogInterface.dismiss())
                .create();

        dialog.show();

        // Sobrescribir click listener del botón positivo para realizar validaciones explícitas antes de cerrar
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Limpiar errores previos
            tilName.setError(null);
            tilPrice.setError(null);
            tilStock.setError(null);

            String name = etProductName.getText() != null ? etProductName.getText().toString().trim() : "";
            String category = spinnerCategory.getSelectedItem() != null ? spinnerCategory.getSelectedItem().toString() : "";
            String priceStr = etProductPrice.getText() != null ? etProductPrice.getText().toString().trim() : "";
            String stockStr = etProductStock.getText() != null ? etProductStock.getText().toString().trim() : "";

            boolean isValid = true;

            // Validación 1: Nombre no vacío
            if (name.isEmpty()) {
                tilName.setError(getString(R.string.error_required));
                isValid = false;
            }

            // Validación 2: Precio mayor a cero
            double price = -1;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    tilPrice.setError(getString(R.string.error_invalid_price));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilPrice.setError(getString(R.string.error_invalid_price));
                isValid = false;
            }

            // Validación 3: Stock entero mayor a cero
            int stock = -1;
            try {
                stock = Integer.parseInt(stockStr);
                if (stock <= 0) {
                    tilStock.setError(getString(R.string.error_invalid_stock));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                tilStock.setError(getString(R.string.error_invalid_stock));
                isValid = false;
            }

            // Si todas las validaciones son correctas, proceder con la BD
            if (isValid) {
                if (isEditMode) {
                    // Actualizar datos del producto
                    productToEdit.setNombre(name);
                    productToEdit.setCategoria(category);
                    productToEdit.setPrecio(price);
                    productToEdit.setStock(stock);

                    int rows = dbHelper.updateProduct(productToEdit);
                    if (rows > 0) {
                        showSnackbar(getString(R.string.msg_product_updated));
                        loadProducts();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.msg_db_error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Insertar nuevo producto
                    Product newProduct = new Product(name, category, price, stock);
                    long id = dbHelper.insertProduct(newProduct);
                    if (id != -1) {
                        showSnackbar(getString(R.string.msg_product_added));
                        loadProducts();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.msg_db_error, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar un producto por ID.
     *
     * @param product Producto a ser eliminado.
     */
    private void showDeleteConfirmationDialog(Product product) {
        String message = getString(R.string.msg_delete_confirm, product.getNombre());

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.title_delete_confirm)
                .setMessage(message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    int rowsDeleted = dbHelper.deleteProduct(product.getId());
                    if (rowsDeleted > 0) {
                        showSnackbar(getString(R.string.msg_product_deleted));
                        loadProducts();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.msg_db_error, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Muestra un mensaje de retroalimentación en la parte inferior de la pantalla utilizando Snackbar.
     */
    private void showSnackbar(String message) {
        View rootView = findViewById(android.R.id.content);
        if (rootView == null) {
            rootView = rvProducts;
        }
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }
}
