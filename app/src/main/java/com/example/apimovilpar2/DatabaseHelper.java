package com.example.apimovilpar2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase SQLiteOpenHelper para gestionar la creación, actualización y operaciones CRUD
 * de la base de datos local SQLite de productos.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    // Nombre de la tabla y columnas
    public static final String TABLE_PRODUCTS = "productos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_CATEGORIA = "categoria";
    public static final String COLUMN_PRECIO = "precio";
    public static final String COLUMN_STOCK = "stock";

    // Sentencia SQL de creación de tabla
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_CATEGORIA + " TEXT NOT NULL, " +
                    COLUMN_PRECIO + " REAL NOT NULL, " +
                    COLUMN_STOCK + " INTEGER NOT NULL" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Crear la tabla de productos
            db.execSQL(TABLE_CREATE);
            Log.d(TAG, "Tabla de productos creada correctamente.");

            // Insertar datos iniciales de prueba (Seed Data)
            insertInitialData(db);
        } catch (SQLException e) {
            Log.e(TAG, "Error al crear la tabla de productos: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
            onCreate(db);
        } catch (SQLException e) {
            Log.e(TAG, "Error al actualizar la base de datos: " + e.getMessage());
        }
    }

    /**
     * Inserta productos iniciales de muestra al crear la base de datos.
     */
    private void insertInitialData(SQLiteDatabase db) {
        Product[] sampleProducts = new Product[]{
                new Product("Laptop ASUS ROG Strix G16", "Laptops", 1499.99, 12),
                new Product("iPhone 15 Pro Max 256GB", "Smartphones", 1199.00, 8),
                new Product("Teclado Mecánico Logitech G Pro", "Periféricos", 129.50, 25),
                new Product("Monitor LG UltraGear 27\" 144Hz", "Monitores", 289.90, 5),
                new Product("Audífonos Sony WH-1000XM5", "Audio", 349.99, 15),
                new Product("Procesador AMD Ryzen 7 7800X3D", "Componentes", 389.00, 4)
        };

        for (Product p : sampleProducts) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NOMBRE, p.getNombre());
            values.put(COLUMN_CATEGORIA, p.getCategoria());
            values.put(COLUMN_PRECIO, p.getPrecio());
            values.put(COLUMN_STOCK, p.getStock());
            db.insert(TABLE_PRODUCTS, null, values);
        }
    }

    // =========================================================================
    // OPERACIONES CRUD (CREATE, READ, UPDATE, DELETE)
    // =========================================================================

    /**
     * OPERACIÓN C: Insertar un nuevo producto en la base de datos.
     *
     * @param product Objeto Producto a insertar.
     * @return ID de la fila insertada o -1 si ocurrió un error.
     */
    public long insertProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = -1;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NOMBRE, product.getNombre());
            values.put(COLUMN_CATEGORIA, product.getCategoria());
            values.put(COLUMN_PRECIO, product.getPrecio());
            values.put(COLUMN_STOCK, product.getStock());

            result = db.insert(TABLE_PRODUCTS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error al insertar producto: " + e.getMessage());
        }
        return result;
    }

    /**
     * OPERACIÓN R: Obtener todos los productos ordenados por nombre.
     *
     * @return Lista de todos los productos.
     */
    public List<Product> getAllProducts() {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_PRODUCTS + " ORDER BY " + COLUMN_NOMBRE + " ASC";

        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NOMBRE);
                int categoryIndex = cursor.getColumnIndexOrThrow(COLUMN_CATEGORIA);
                int priceIndex = cursor.getColumnIndexOrThrow(COLUMN_PRECIO);
                int stockIndex = cursor.getColumnIndexOrThrow(COLUMN_STOCK);

                do {
                    Product product = new Product(
                            cursor.getInt(idIndex),
                            cursor.getString(nameIndex),
                            cursor.getString(categoryIndex),
                            cursor.getDouble(priceIndex),
                            cursor.getInt(stockIndex)
                    );
                    productList.add(product);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al listar productos: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return productList;
    }

    /**
     * OPERACIÓN R (FILTRO/BÚSQUEDA): Filtrar productos por nombre y/o categoría.
     *
     * @param query    Texto a buscar en el nombre del producto.
     * @param category Categoría seleccionada para filtrar ("Todas las categorías" para no filtrar por categoría).
     * @return Lista filtrada de productos.
     */
    public List<Product> searchProducts(String query, String category) {
        List<Product> productList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder selectionBuilder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            selectionBuilder.append(COLUMN_NOMBRE).append(" LIKE ?");
            selectionArgsList.add("%" + query.trim() + "%");
        }

        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("Todas las categorías")) {
            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" AND ");
            }
            selectionBuilder.append(COLUMN_CATEGORIA).append(" = ?");
            selectionArgsList.add(category);
        }

        String selection = selectionBuilder.length() > 0 ? selectionBuilder.toString() : null;
        String[] selectionArgs = selectionArgsList.isEmpty() ? null : selectionArgsList.toArray(new String[0]);

        Cursor cursor = null;
        try {
            cursor = db.query(
                    TABLE_PRODUCTS,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    COLUMN_NOMBRE + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(COLUMN_NOMBRE);
                int categoryIndex = cursor.getColumnIndexOrThrow(COLUMN_CATEGORIA);
                int priceIndex = cursor.getColumnIndexOrThrow(COLUMN_PRECIO);
                int stockIndex = cursor.getColumnIndexOrThrow(COLUMN_STOCK);

                do {
                    Product product = new Product(
                            cursor.getInt(idIndex),
                            cursor.getString(nameIndex),
                            cursor.getString(categoryIndex),
                            cursor.getDouble(priceIndex),
                            cursor.getInt(stockIndex)
                    );
                    productList.add(product);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al buscar productos: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return productList;
    }

    /**
     * OPERACIÓN U: Actualizar los datos de un producto existente.
     *
     * @param product Objeto Producto con los datos actualizados e ID válido.
     * @return Cantidad de filas afectadas.
     */
    public int updateProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NOMBRE, product.getNombre());
            values.put(COLUMN_CATEGORIA, product.getCategoria());
            values.put(COLUMN_PRECIO, product.getPrecio());
            values.put(COLUMN_STOCK, product.getStock());

            rowsAffected = db.update(
                    TABLE_PRODUCTS,
                    values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(product.getId())}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar producto: " + e.getMessage());
        }
        return rowsAffected;
    }

    /**
     * OPERACIÓN D: Eliminar un producto por su ID.
     *
     * @param id ID del producto a eliminar.
     * @return Cantidad de filas eliminadas.
     */
    public int deleteProduct(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = 0;
        try {
            rowsDeleted = db.delete(
                    TABLE_PRODUCTS,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar producto: " + e.getMessage());
        }
        return rowsDeleted;
    }
}
