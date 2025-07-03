using DXApplication1.Models;
using DXApplication1.BusinessLogicLayer;
using DXApplication1.DataAccessLayer;
using DevExpress.XtraEditors;
using DevExpress.XtraGrid;
using DevExpress.XtraGrid.Views.Grid;
using System.ComponentModel;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// نموذج إدارة المنتجات - Products Management Form
    /// </summary>
    public partial class ProductsForm : XtraForm, IDisposable
    {
        private IProductService _productService = null!;
        private BindingList<Product> _products = null!;

        // DevExpress Controls
        private GridControl gridProducts = null!;
        private GridView gridViewProducts = null!;
        private SimpleButton btnAdd = null!;
        private SimpleButton btnEdit = null!;
        private SimpleButton btnDelete = null!;
        private SimpleButton btnRefresh = null!;
        private SimpleButton btnTileView = null!;
        private SimpleButton btnAnalytics = null!;
        private SimpleButton btnClose = null!;
        private TextEdit txtSearch = null!;
        private LabelControl lblSearch = null!;
        private ComboBoxEdit cmbCategory = null!;
        private LabelControl lblCategory = null!;
        private PanelControl panelTop = null!;
        private PanelControl panelBottom = null!;

        public ProductsForm()
        {
            try
            {
                InitializeComponent();
                InitializeServices();
                SetupForm();

                // Load products after form is fully initialized
                this.Load += ProductsForm_Load;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"خطأ في إنشاء نموذج المنتجات: {ex}");
                XtraMessageBox.Show($"خطأ في تهيئة نموذج المنتجات:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private async void ProductsForm_Load(object? sender, EventArgs e)
        {
            // Test database connection first
            if (await TestDatabaseConnectionAsync())
            {
                await LoadProductsAsync();
            }
            else
            {
                XtraMessageBox.Show("فشل في الاتصال بقاعدة البيانات. يرجى التحقق من إعدادات الاتصال.", "خطأ في الاتصال",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void InitializeServices()
        {
            try
            {
                // إنشاء الخدمات - Initialize services
                // Note: We'll create DbContext instances per operation to avoid threading issues
                _productService = new ProductService();

                // Log for debugging
                System.Diagnostics.Debug.WriteLine("تم تهيئة خدمات المنتجات بنجاح");
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"خطأ في تهيئة خدمات المنتجات: {ex}");
                throw new InvalidOperationException("فشل في تهيئة خدمات المنتجات", ex);
            }
        }

        private async Task<bool> TestDatabaseConnectionAsync()
        {
            try
            {
                // Test the connection by trying to connect to database
                using var context = new SalesDbContext();
                await context.Database.CanConnectAsync();
                System.Diagnostics.Debug.WriteLine("تم الاتصال بقاعدة البيانات بنجاح");
                return true;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"فشل في الاتصال بقاعدة البيانات: {ex}");
                return false;
            }
        }

        private void InitializeComponent()
        {
            this.gridProducts = new DevExpress.XtraGrid.GridControl();
            this.gridViewProducts = new DevExpress.XtraGrid.Views.Grid.GridView();
            this.btnAdd = new DevExpress.XtraEditors.SimpleButton();
            this.btnEdit = new DevExpress.XtraEditors.SimpleButton();
            this.btnDelete = new DevExpress.XtraEditors.SimpleButton();
            this.btnRefresh = new DevExpress.XtraEditors.SimpleButton();
            this.btnTileView = new DevExpress.XtraEditors.SimpleButton();
            this.btnAnalytics = new DevExpress.XtraEditors.SimpleButton();
            this.btnClose = new DevExpress.XtraEditors.SimpleButton();
            this.txtSearch = new DevExpress.XtraEditors.TextEdit();
            this.lblSearch = new DevExpress.XtraEditors.LabelControl();
            this.cmbCategory = new DevExpress.XtraEditors.ComboBoxEdit();
            this.lblCategory = new DevExpress.XtraEditors.LabelControl();
            this.panelTop = new DevExpress.XtraEditors.PanelControl();
            this.panelBottom = new DevExpress.XtraEditors.PanelControl();

            ((System.ComponentModel.ISupportInitialize)(this.gridProducts)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridViewProducts)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtSearch.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.cmbCategory.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelTop)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelBottom)).BeginInit();
            this.panelTop.SuspendLayout();
            this.panelBottom.SuspendLayout();
            this.SuspendLayout();

            // panelTop
            this.panelTop.Controls.Add(this.cmbCategory);
            this.panelTop.Controls.Add(this.lblCategory);
            this.panelTop.Controls.Add(this.txtSearch);
            this.panelTop.Controls.Add(this.lblSearch);
            this.panelTop.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelTop.Location = new System.Drawing.Point(0, 0);
            this.panelTop.Name = "panelTop";
            this.panelTop.Size = new System.Drawing.Size(1200, 60);
            this.panelTop.TabIndex = 0;

            // lblSearch
            this.lblSearch.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F);
            this.lblSearch.Location = new System.Drawing.Point(20, 20);
            this.lblSearch.Name = "lblSearch";
            this.lblSearch.Size = new System.Drawing.Size(35, 19);
            this.lblSearch.TabIndex = 0;
            this.lblSearch.Text = "البحث:";

            // txtSearch
            this.txtSearch.Location = new System.Drawing.Point(70, 17);
            this.txtSearch.Name = "txtSearch";
            this.txtSearch.Properties.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F);
            this.txtSearch.Properties.Appearance.Options.UseFont = true;
            this.txtSearch.Size = new System.Drawing.Size(300, 24);
            this.txtSearch.TabIndex = 1;
            this.txtSearch.EditValueChanged += new System.EventHandler(this.txtSearch_EditValueChanged);

            // lblCategory
            this.lblCategory.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F);
            this.lblCategory.Location = new System.Drawing.Point(400, 20);
            this.lblCategory.Name = "lblCategory";
            this.lblCategory.Size = new System.Drawing.Size(35, 19);
            this.lblCategory.TabIndex = 2;
            this.lblCategory.Text = "الفئة:";

            // cmbCategory
            this.cmbCategory.Location = new System.Drawing.Point(450, 17);
            this.cmbCategory.Name = "cmbCategory";
            this.cmbCategory.Properties.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F);
            this.cmbCategory.Properties.Appearance.Options.UseFont = true;
            this.cmbCategory.Size = new System.Drawing.Size(200, 24);
            this.cmbCategory.TabIndex = 3;
            this.cmbCategory.SelectedIndexChanged += new System.EventHandler(this.cmbCategory_SelectedIndexChanged);

            // gridProducts
            this.gridProducts.Dock = System.Windows.Forms.DockStyle.Fill;
            this.gridProducts.Location = new System.Drawing.Point(0, 60);
            this.gridProducts.MainView = this.gridViewProducts;
            this.gridProducts.Name = "gridProducts";
            this.gridProducts.Size = new System.Drawing.Size(1200, 440);
            this.gridProducts.TabIndex = 1;
            this.gridProducts.ViewCollection.AddRange(new DevExpress.XtraGrid.Views.Base.BaseView[] { this.gridViewProducts });

            // gridViewProducts
            this.gridViewProducts.GridControl = this.gridProducts;
            this.gridViewProducts.Name = "gridViewProducts";
            this.gridViewProducts.OptionsView.ShowGroupPanel = false;
            this.gridViewProducts.OptionsView.ColumnAutoWidth = false;
            this.gridViewProducts.OptionsSelection.EnableAppearanceFocusedCell = false;
            this.gridViewProducts.OptionsSelection.MultiSelect = false;
            this.gridViewProducts.FocusedRowChanged += new DevExpress.XtraGrid.Views.Base.FocusedRowChangedEventHandler(this.gridViewProducts_FocusedRowChanged);
            this.gridViewProducts.DoubleClick += new System.EventHandler(this.gridViewProducts_DoubleClick);

            // panelBottom
            this.panelBottom.Controls.Add(this.btnClose);
            this.panelBottom.Controls.Add(this.btnAnalytics);
            this.panelBottom.Controls.Add(this.btnTileView);
            this.panelBottom.Controls.Add(this.btnRefresh);
            this.panelBottom.Controls.Add(this.btnDelete);
            this.panelBottom.Controls.Add(this.btnEdit);
            this.panelBottom.Controls.Add(this.btnAdd);
            this.panelBottom.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.panelBottom.Location = new System.Drawing.Point(0, 500);
            this.panelBottom.Name = "panelBottom";
            this.panelBottom.Size = new System.Drawing.Size(1200, 60);
            this.panelBottom.TabIndex = 2;

            // btnAdd
            this.btnAdd.Appearance.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.btnAdd.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F, System.Drawing.FontStyle.Bold);
            this.btnAdd.Appearance.ForeColor = System.Drawing.Color.White;
            this.btnAdd.Appearance.Options.UseBackColor = true;
            this.btnAdd.Appearance.Options.UseFont = true;
            this.btnAdd.Appearance.Options.UseForeColor = true;
            this.btnAdd.Location = new System.Drawing.Point(20, 15);
            this.btnAdd.Name = "btnAdd";
            this.btnAdd.Size = new System.Drawing.Size(100, 30);
            this.btnAdd.TabIndex = 0;
            this.btnAdd.Text = "إضافة";
            this.btnAdd.Click += new System.EventHandler(this.btnAdd_Click);

            // btnEdit
            this.btnEdit.Appearance.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(40)))), ((int)(((byte)(167)))), ((int)(((byte)(69)))));
            this.btnEdit.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F, System.Drawing.FontStyle.Bold);
            this.btnEdit.Appearance.ForeColor = System.Drawing.Color.White;
            this.btnEdit.Appearance.Options.UseBackColor = true;
            this.btnEdit.Appearance.Options.UseFont = true;
            this.btnEdit.Appearance.Options.UseForeColor = true;
            this.btnEdit.Location = new System.Drawing.Point(130, 15);
            this.btnEdit.Name = "btnEdit";
            this.btnEdit.Size = new System.Drawing.Size(100, 30);
            this.btnEdit.TabIndex = 1;
            this.btnEdit.Text = "تعديل";
            this.btnEdit.Click += new System.EventHandler(this.btnEdit_Click);

            // btnDelete
            this.btnDelete.Appearance.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(220)))), ((int)(((byte)(53)))), ((int)(((byte)(69)))));
            this.btnDelete.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F, System.Drawing.FontStyle.Bold);
            this.btnDelete.Appearance.ForeColor = System.Drawing.Color.White;
            this.btnDelete.Appearance.Options.UseBackColor = true;
            this.btnDelete.Appearance.Options.UseFont = true;
            this.btnDelete.Appearance.Options.UseForeColor = true;
            this.btnDelete.Location = new System.Drawing.Point(240, 15);
            this.btnDelete.Name = "btnDelete";
            this.btnDelete.Size = new System.Drawing.Size(100, 30);
            this.btnDelete.TabIndex = 2;
            this.btnDelete.Text = "حذف";
            this.btnDelete.Click += new System.EventHandler(this.btnDelete_Click);

            // btnRefresh
            this.btnRefresh.Appearance.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(108)))), ((int)(((byte)(117)))), ((int)(((byte)(125)))));
            this.btnRefresh.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F, System.Drawing.FontStyle.Bold);
            this.btnRefresh.Appearance.ForeColor = System.Drawing.Color.White;
            this.btnRefresh.Appearance.Options.UseBackColor = true;
            this.btnRefresh.Appearance.Options.UseFont = true;
            this.btnRefresh.Appearance.Options.UseForeColor = true;
            this.btnRefresh.Location = new System.Drawing.Point(350, 15);
            this.btnRefresh.Name = "btnRefresh";
            this.btnRefresh.Size = new System.Drawing.Size(100, 30);
            this.btnRefresh.TabIndex = 3;
            this.btnRefresh.Text = "تحديث";
            this.btnRefresh.Click += new System.EventHandler(this.btnRefresh_Click);

            // btnTileView
            this.btnTileView.Appearance.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(255)))), ((int)(((byte)(193)))), ((int)(((byte)(7)))));
            this.btnTileView.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F, System.Drawing.FontStyle.Bold);
            this.btnTileView.Appearance.ForeColor = System.Drawing.Color.Black;
            this.btnTileView.Appearance.Options.UseBackColor = true;
            this.btnTileView.Appearance.Options.UseFont = true;
            this.btnTileView.Appearance.Options.UseForeColor = true;
            this.btnTileView.Location = new System.Drawing.Point(460, 15);
            this.btnTileView.Name = "btnTileView";
            this.btnTileView.Size = new System.Drawing.Size(100, 30);
            this.btnTileView.TabIndex = 4;
            this.btnTileView.Text = "عرض البلاطات";
            this.btnTileView.Click += new System.EventHandler(this.btnTileView_Click);

            // btnAnalytics
            this.btnAnalytics.Appearance.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(102)))), ((int)(((byte)(16)))), ((int)(((byte)(242)))));
            this.btnAnalytics.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F, System.Drawing.FontStyle.Bold);
            this.btnAnalytics.Appearance.ForeColor = System.Drawing.Color.White;
            this.btnAnalytics.Appearance.Options.UseBackColor = true;
            this.btnAnalytics.Appearance.Options.UseFont = true;
            this.btnAnalytics.Appearance.Options.UseForeColor = true;
            this.btnAnalytics.Location = new System.Drawing.Point(570, 15);
            this.btnAnalytics.Name = "btnAnalytics";
            this.btnAnalytics.Size = new System.Drawing.Size(100, 30);
            this.btnAnalytics.TabIndex = 5;
            this.btnAnalytics.Text = "التحليلات";
            this.btnAnalytics.Click += new System.EventHandler(this.btnAnalytics_Click);

            // btnClose
            this.btnClose.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F);
            this.btnClose.Location = new System.Drawing.Point(1080, 15);
            this.btnClose.Name = "btnClose";
            this.btnClose.Size = new System.Drawing.Size(100, 30);
            this.btnClose.TabIndex = 6;
            this.btnClose.Text = "إغلاق";
            this.btnClose.Click += new System.EventHandler(this.btnClose_Click);

            // ProductsForm
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1200, 560);
            this.Controls.Add(this.gridProducts);
            this.Controls.Add(this.panelTop);
            this.Controls.Add(this.panelBottom);
            this.IconOptions.ShowIcon = false;
            this.Name = "ProductsForm";
            this.RightToLeft = System.Windows.Forms.RightToLeft.Yes;
            this.RightToLeftLayout = true;
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "إدارة المنتجات";
            this.WindowState = System.Windows.Forms.FormWindowState.Maximized;

            ((System.ComponentModel.ISupportInitialize)(this.gridProducts)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridViewProducts)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtSearch.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.cmbCategory.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelTop)).EndInit();
            this.panelTop.ResumeLayout(false);
            this.panelTop.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.panelBottom)).EndInit();
            this.panelBottom.ResumeLayout(false);
            this.ResumeLayout(false);
        }

        private void SetupForm()
        {
            // إعداد النموذج - Setup form
            this.Text = "إدارة المنتجات - Products Management";
            this.WindowState = FormWindowState.Maximized;
            this.StartPosition = FormStartPosition.CenterScreen;

            // RTL support
            this.RightToLeft = RightToLeft.Yes;
            this.RightToLeftLayout = true;

            // Modern UI styling
            this.LookAndFeel.UseDefaultLookAndFeel = false;
            this.LookAndFeel.SkinName = "WXI";
            this.Appearance.BackColor = Color.FromArgb(248, 249, 250);

            // Set Arabic font with better readability
            this.Font = new Font("Segoe UI", 9.5F, FontStyle.Regular);

            // إعداد الشبكة - Setup grid
            SetupGrid();

            // إعداد فئات المنتجات - Setup categories
            LoadCategories();

            // تحديث حالة الأزرار - Update button states
            UpdateButtonStates();
        }

        private void SetupGrid()
        {
            // إعداد أعمدة الشبكة - Setup grid columns
            gridViewProducts.Columns.Clear();

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "Id",
                Caption = "الرقم",
                Visible = false
            });

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "ProductName",
                Caption = "اسم المنتج",
                Width = 200
            });

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "ProductCode",
                Caption = "كود المنتج",
                Width = 120
            });

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "Barcode",
                Caption = "الباركود",
                Width = 120
            });

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "Category.CategoryName",
                Caption = "الفئة",
                Width = 120
            });

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "PurchasePrice",
                Caption = "سعر الشراء",
                Width = 100,
                DisplayFormat = { FormatType = DevExpress.Utils.FormatType.Numeric, FormatString = "N2" }
            });

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "SalePrice",
                Caption = "سعر البيع",
                Width = 100,
                DisplayFormat = { FormatType = DevExpress.Utils.FormatType.Numeric, FormatString = "N2" }
            });

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "StockQuantity",
                Caption = "الكمية",
                Width = 80
            });

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "MinimumStock",
                Caption = "الحد الأدنى",
                Width = 80
            });

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "Unit",
                Caption = "الوحدة",
                Width = 80
            });

            gridViewProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "IsActive",
                Caption = "نشط",
                Width = 60
            });

            // إعداد خصائص الشبكة - Setup grid properties
            gridViewProducts.OptionsView.ShowAutoFilterRow = true;
            gridViewProducts.OptionsSelection.EnableAppearanceFocusedCell = false;
            gridViewProducts.OptionsSelection.MultiSelect = false;
            gridViewProducts.OptionsView.ColumnAutoWidth = false;
            gridViewProducts.BestFitColumns();
        }

        private async void LoadCategories()
        {
            try
            {
                var categories = await _productService.GetAllCategoriesAsync();

                cmbCategory.Properties.Items.Clear();
                cmbCategory.Properties.Items.Add(new { Text = "جميع الفئات", Value = -1 });

                foreach (var category in categories)
                {
                    cmbCategory.Properties.Items.Add(new { Text = category.CategoryName, Value = category.Id });
                }

                // DevExpress ComboBoxEdit doesn't use DisplayMember/ValueMember like WinForms
                cmbCategory.SelectedIndex = 0;
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في تحميل الفئات:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private async Task LoadProductsAsync()
        {
            try
            {
                var products = await _productService.GetAllProductsAsync();
                _products = new BindingList<Product>(products.ToList());
                gridProducts.DataSource = _products;

                UpdateButtonStates();
            }
            catch (Exception ex)
            {
                LoadingHelper.ShowError($"خطأ في تحميل بيانات المنتجات:\n{ex.Message}\n\nتفاصيل الخطأ:\n{ex.InnerException?.Message}");

                // Log the full exception for debugging
                System.Diagnostics.Debug.WriteLine($"خطأ في تحميل المنتجات: {ex}");
                throw; // Re-throw to be handled by LoadingHelper
            }
        }

        private void UpdateButtonStates()
        {
            var hasSelection = gridViewProducts.FocusedRowHandle >= 0;
            btnEdit.Enabled = hasSelection;
            btnDelete.Enabled = hasSelection;
        }

        private Product? GetSelectedProduct()
        {
            if (gridViewProducts.FocusedRowHandle >= 0)
                return gridViewProducts.GetRow(gridViewProducts.FocusedRowHandle) as Product;
            return null;
        }

        // Event Handlers
        private void txtSearch_EditValueChanged(object? sender, EventArgs e)
        {
            PerformSearch();
        }

        private async void PerformSearch()
        {
            try
            {
                var searchTerm = txtSearch.Text?.Trim();

                if (string.IsNullOrWhiteSpace(searchTerm))
                {
                    await LoadProductsAsync();
                    return;
                }

                this.Cursor = Cursors.WaitCursor;

                var products = await _productService.SearchProductsAsync(searchTerm);
                _products = new BindingList<Product>(products.ToList());
                gridProducts.DataSource = _products;

                UpdateButtonStates();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في البحث:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                this.Cursor = Cursors.Default;
            }
        }

        private async void cmbCategory_SelectedIndexChanged(object? sender, EventArgs e)
        {
            try
            {
                var selectedItem = (dynamic)cmbCategory.SelectedItem;
                if (selectedItem == null) return;

                this.Cursor = Cursors.WaitCursor;

                IEnumerable<Product> products;
                if (selectedItem.Value == -1)
                {
                    products = await _productService.GetAllProductsAsync();
                }
                else
                {
                    products = await _productService.GetProductsByCategoryAsync(selectedItem.Value);
                }

                _products = new BindingList<Product>(products.ToList());
                gridProducts.DataSource = _products;

                UpdateButtonStates();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في تصفية المنتجات:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                this.Cursor = Cursors.Default;
            }
        }

        private void gridViewProducts_FocusedRowChanged(object sender, DevExpress.XtraGrid.Views.Base.FocusedRowChangedEventArgs e)
        {
            UpdateButtonStates();
        }

        private void gridViewProducts_DoubleClick(object? sender, EventArgs e)
        {
            if (btnEdit.Enabled)
                btnEdit_Click(sender, e);
        }

        private async void btnAdd_Click(object? sender, EventArgs e)
        {
            try
            {
                using var addForm = new ProductAddEditForm(_productService);
                if (addForm.ShowDialog() == DialogResult.OK)
                {
                    await LoadProductsAsync();
                    LoadingHelper.ShowSuccess("تم إضافة المنتج بنجاح", "إضافة منتج");
                }
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في فتح نموذج الإضافة:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private async void btnEdit_Click(object? sender, EventArgs e)
        {
            try
            {
                var selectedProduct = GetSelectedProduct();
                if (selectedProduct == null)
                {
                    XtraMessageBox.Show("يرجى اختيار منتج للتعديل", "تنبيه",
                        MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                using var editForm = new ProductAddEditForm(_productService, selectedProduct);
                if (editForm.ShowDialog() == DialogResult.OK)
                {
                    await LoadProductsAsync();
                    LoadingHelper.ShowSuccess("تم تحديث المنتج بنجاح", "تعديل منتج");
                }
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في فتح نموذج التعديل:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private async void btnDelete_Click(object? sender, EventArgs e)
        {
            try
            {
                var selectedProduct = GetSelectedProduct();
                if (selectedProduct == null)
                {
                    XtraMessageBox.Show("يرجى اختيار منتج للحذف", "تنبيه",
                        MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                var result = XtraMessageBox.Show(
                    $"هل أنت متأكد من حذف المنتج '{selectedProduct.ProductName}'؟\nهذا الإجراء لا يمكن التراجع عنه.",
                    "تأكيد الحذف", MessageBoxButtons.YesNo, MessageBoxIcon.Question);

                if (result == DialogResult.Yes)
                {
                    this.Cursor = Cursors.WaitCursor;

                    var success = await _productService.DeleteProductAsync(selectedProduct.Id);
                    if (success)
                    {
                        XtraMessageBox.Show("تم حذف المنتج بنجاح", "نجح",
                            MessageBoxButtons.OK, MessageBoxIcon.Information);
                        await LoadProductsAsync();
                    }
                    else
                    {
                        XtraMessageBox.Show("فشل في حذف المنتج", "خطأ",
                            MessageBoxButtons.OK, MessageBoxIcon.Error);
                    }
                }
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في حذف المنتج:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                this.Cursor = Cursors.Default;
            }
        }

        private async void btnRefresh_Click(object? sender, EventArgs e)
        {
            await LoadingHelper.ExecuteWithLoadingAsync(async () =>
            {
                txtSearch.Text = "";
                cmbCategory.SelectedIndex = 0;
                await LoadProductsAsync();
                LoadingHelper.ShowSuccess("تم تحديث بيانات المنتجات بنجاح", "تحديث");
            }, "جاري تحديث بيانات المنتجات...", this);
        }

        private void btnTileView_Click(object? sender, EventArgs e)
        {
            try
            {
                using var tileForm = new ProductTileViewForm(_productService);
                tileForm.ShowDialog();
            }
            catch (Exception ex)
            {
                ProductErrorHandler.HandleGeneralError(ex, "فتح عرض البلاطات");
            }
        }

        private void btnAnalytics_Click(object? sender, EventArgs e)
        {
            try
            {
                var dashboardService = new DashboardService();
                using var analyticsForm = new ProductAnalyticsForm(_productService, dashboardService);
                analyticsForm.ShowDialog();
            }
            catch (Exception ex)
            {
                ProductErrorHandler.HandleGeneralError(ex, "فتح التحليلات");
            }
        }

        private void btnClose_Click(object? sender, EventArgs e)
        {
            this.Close();
        }

        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                // Dispose managed resources
                if (_productService is IDisposable disposableService)
                {
                    disposableService.Dispose();
                }
            }
            base.Dispose(disposing);
        }
    }
}
