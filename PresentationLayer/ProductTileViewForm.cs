using DXApplication1.Models;
using DXApplication1.BusinessLogicLayer;
using DevExpress.XtraEditors;
using DevExpress.XtraGrid;
using DevExpress.XtraGrid.Views.Tile;
using System.ComponentModel;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// نموذج عرض المنتجات بالبلاطات - Product Tile View Form
    /// </summary>
    public partial class ProductTileViewForm : XtraForm
    {
        private readonly IProductService _productService;
        private BindingList<Product> _products = null!;
        private BindingList<Category> _categories = null!;

        // DevExpress Controls
        private GridControl gridProducts = null!;
        private TileView tileViewProducts = null!;
        private PanelControl panelTop = null!;
        private PanelControl panelBottom = null!;
        private TextEdit txtSearch = null!;
        private LabelControl lblSearch = null!;
        private ComboBoxEdit cmbCategory = null!;
        private LabelControl lblCategory = null!;
        private SimpleButton btnAdd = null!;
        private SimpleButton btnEdit = null!;
        private SimpleButton btnDelete = null!;
        private SimpleButton btnRefresh = null!;
        private SimpleButton btnClose = null!;
        private SimpleButton btnGridView = null!;

        public ProductTileViewForm(IProductService productService)
        {
            _productService = productService ?? throw new ArgumentNullException(nameof(productService));
            
            InitializeComponent();
            SetupForm();
            _ = LoadDataAsync();
        }

        private void SetupForm()
        {
            // إعداد النموذج - Setup form
            this.Text = "عرض المنتجات - Products Overview";
            this.WindowState = FormWindowState.Maximized;
            this.StartPosition = FormStartPosition.CenterScreen;

            // RTL support
            this.RightToLeft = RightToLeft.Yes;
            this.RightToLeftLayout = true;

            // Modern UI styling
            this.LookAndFeel.UseDefaultLookAndFeel = false;
            this.LookAndFeel.SkinName = "WXI";
            this.Appearance.BackColor = Color.FromArgb(248, 249, 250);

            // Set Arabic font
            this.Font = new Font("Segoe UI", 9.5F, FontStyle.Regular);

            // إعداد عرض البلاطات - Setup tile view
            SetupTileView();
        }

        private void SetupTileView()
        {
            // Configure tile view properties - use basic settings
            tileViewProducts.OptionsSelection.MultiSelect = false;

            // Create tile template
            CreateTileTemplate();

            // Event handlers
            tileViewProducts.FocusedRowChanged += TileViewProducts_FocusedRowChanged;
            tileViewProducts.DoubleClick += TileViewProducts_DoubleClick;
        }

        private void CreateTileTemplate()
        {
            // Configure basic tile view settings - use simple approach
            // The TileView will display data using default formatting
            // We can customize appearance through events if needed
        }

        private async Task LoadDataAsync()
        {
            try
            {
                // Load categories
                await LoadCategoriesAsync();

                // Load products
                await LoadProductsAsync();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في تحميل البيانات:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private async Task LoadCategoriesAsync()
        {
            try
            {
                var categories = await _productService.GetAllCategoriesAsync();
                _categories = new BindingList<Category>(categories.ToList());

                cmbCategory.Properties.Items.Clear();
                cmbCategory.Properties.Items.Add(new { Text = "جميع الفئات", Value = -1 });

                foreach (var category in _categories)
                {
                    cmbCategory.Properties.Items.Add(new { Text = category.CategoryName, Value = category.Id });
                }

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
                LoadingHelper.ShowError($"خطأ في تحميل بيانات المنتجات:\n{ex.Message}");
            }
        }

        private void UpdateButtonStates()
        {
            var hasSelection = tileViewProducts.FocusedRowHandle >= 0;
            btnEdit.Enabled = hasSelection;
            btnDelete.Enabled = hasSelection;
        }

        private Product? GetSelectedProduct()
        {
            if (tileViewProducts.FocusedRowHandle >= 0)
                return tileViewProducts.GetRow(tileViewProducts.FocusedRowHandle) as Product;
            return null;
        }

        // Event Handlers
        private void TileViewProducts_FocusedRowChanged(object sender, DevExpress.XtraGrid.Views.Base.FocusedRowChangedEventArgs e)
        {
            UpdateButtonStates();
        }

        private void TileViewProducts_DoubleClick(object? sender, EventArgs e)
        {
            if (btnEdit.Enabled)
                btnEdit_Click(sender, e);
        }

        private async void txtSearch_EditValueChanged(object? sender, EventArgs e)
        {
            await PerformSearchAsync();
        }

        private async Task PerformSearchAsync()
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

        private void btnGridView_Click(object? sender, EventArgs e)
        {
            try
            {
                using var gridForm = new ProductsForm();
                gridForm.ShowDialog();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في فتح عرض الشبكة:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void btnClose_Click(object? sender, EventArgs e)
        {
            this.Close();
        }

        private void InitializeComponent()
        {
            this.gridProducts = new GridControl();
            this.tileViewProducts = new TileView();
            this.panelTop = new PanelControl();
            this.panelBottom = new PanelControl();
            this.txtSearch = new TextEdit();
            this.lblSearch = new LabelControl();
            this.cmbCategory = new ComboBoxEdit();
            this.lblCategory = new LabelControl();
            this.btnAdd = new SimpleButton();
            this.btnEdit = new SimpleButton();
            this.btnDelete = new SimpleButton();
            this.btnRefresh = new SimpleButton();
            this.btnGridView = new SimpleButton();
            this.btnClose = new SimpleButton();

            ((System.ComponentModel.ISupportInitialize)(this.gridProducts)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.tileViewProducts)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelTop)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelBottom)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtSearch.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.cmbCategory.Properties)).BeginInit();
            this.panelTop.SuspendLayout();
            this.panelBottom.SuspendLayout();
            this.SuspendLayout();

            // panelTop
            this.panelTop.Controls.Add(this.cmbCategory);
            this.panelTop.Controls.Add(this.lblCategory);
            this.panelTop.Controls.Add(this.txtSearch);
            this.panelTop.Controls.Add(this.lblSearch);
            this.panelTop.Dock = DockStyle.Top;
            this.panelTop.Location = new Point(0, 0);
            this.panelTop.Name = "panelTop";
            this.panelTop.Size = new Size(1200, 60);
            this.panelTop.TabIndex = 0;

            // lblSearch
            this.lblSearch.Appearance.Font = new Font("Segoe UI", 10F);
            this.lblSearch.Location = new Point(20, 20);
            this.lblSearch.Name = "lblSearch";
            this.lblSearch.Size = new Size(35, 19);
            this.lblSearch.TabIndex = 0;
            this.lblSearch.Text = "البحث:";

            // txtSearch
            this.txtSearch.Location = new Point(70, 17);
            this.txtSearch.Name = "txtSearch";
            this.txtSearch.Properties.Appearance.Font = new Font("Segoe UI", 10F);
            this.txtSearch.Properties.Appearance.Options.UseFont = true;
            this.txtSearch.Size = new Size(300, 24);
            this.txtSearch.TabIndex = 1;
            this.txtSearch.EditValueChanged += txtSearch_EditValueChanged;

            // lblCategory
            this.lblCategory.Appearance.Font = new Font("Segoe UI", 10F);
            this.lblCategory.Location = new Point(400, 20);
            this.lblCategory.Name = "lblCategory";
            this.lblCategory.Size = new Size(35, 19);
            this.lblCategory.TabIndex = 2;
            this.lblCategory.Text = "الفئة:";

            // cmbCategory
            this.cmbCategory.Location = new Point(450, 17);
            this.cmbCategory.Name = "cmbCategory";
            this.cmbCategory.Properties.Appearance.Font = new Font("Segoe UI", 10F);
            this.cmbCategory.Properties.Appearance.Options.UseFont = true;
            this.cmbCategory.Size = new Size(200, 24);
            this.cmbCategory.TabIndex = 3;
            this.cmbCategory.SelectedIndexChanged += cmbCategory_SelectedIndexChanged;

            // gridProducts
            this.gridProducts.Dock = DockStyle.Fill;
            this.gridProducts.Location = new Point(0, 60);
            this.gridProducts.MainView = this.tileViewProducts;
            this.gridProducts.Name = "gridProducts";
            this.gridProducts.Size = new Size(1200, 440);
            this.gridProducts.TabIndex = 1;
            this.gridProducts.ViewCollection.AddRange(new DevExpress.XtraGrid.Views.Base.BaseView[] { this.tileViewProducts });

            // tileViewProducts
            this.tileViewProducts.GridControl = this.gridProducts;
            this.tileViewProducts.Name = "tileViewProducts";

            // panelBottom
            this.panelBottom.Controls.Add(this.btnClose);
            this.panelBottom.Controls.Add(this.btnGridView);
            this.panelBottom.Controls.Add(this.btnRefresh);
            this.panelBottom.Controls.Add(this.btnDelete);
            this.panelBottom.Controls.Add(this.btnEdit);
            this.panelBottom.Controls.Add(this.btnAdd);
            this.panelBottom.Dock = DockStyle.Bottom;
            this.panelBottom.Location = new Point(0, 500);
            this.panelBottom.Name = "panelBottom";
            this.panelBottom.Size = new Size(1200, 60);
            this.panelBottom.TabIndex = 2;

            // Configure buttons
            ConfigureButtons();

            // ProductTileViewForm
            this.AutoScaleDimensions = new SizeF(6F, 13F);
            this.AutoScaleMode = AutoScaleMode.Font;
            this.ClientSize = new Size(1200, 560);
            this.Controls.Add(this.gridProducts);
            this.Controls.Add(this.panelTop);
            this.Controls.Add(this.panelBottom);
            this.IconOptions.ShowIcon = false;
            this.Name = "ProductTileViewForm";
            this.RightToLeft = RightToLeft.Yes;
            this.RightToLeftLayout = true;
            this.StartPosition = FormStartPosition.CenterParent;
            this.Text = "عرض المنتجات";
            this.WindowState = FormWindowState.Maximized;

            ((System.ComponentModel.ISupportInitialize)(this.gridProducts)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.tileViewProducts)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelTop)).EndInit();
            this.panelTop.ResumeLayout(false);
            this.panelTop.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.panelBottom)).EndInit();
            this.panelBottom.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.txtSearch.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.cmbCategory.Properties)).EndInit();
            this.ResumeLayout(false);
        }

        private void ConfigureButtons()
        {
            // btnAdd
            this.btnAdd.Appearance.BackColor = Color.FromArgb(0, 122, 204);
            this.btnAdd.Appearance.Font = new Font("Segoe UI", 10F, FontStyle.Bold);
            this.btnAdd.Appearance.ForeColor = Color.White;
            this.btnAdd.Appearance.Options.UseBackColor = true;
            this.btnAdd.Appearance.Options.UseFont = true;
            this.btnAdd.Appearance.Options.UseForeColor = true;
            this.btnAdd.Location = new Point(20, 15);
            this.btnAdd.Name = "btnAdd";
            this.btnAdd.Size = new Size(100, 30);
            this.btnAdd.TabIndex = 0;
            this.btnAdd.Text = "إضافة";
            this.btnAdd.Click += btnAdd_Click;

            // btnEdit
            this.btnEdit.Appearance.BackColor = Color.FromArgb(40, 167, 69);
            this.btnEdit.Appearance.Font = new Font("Segoe UI", 10F, FontStyle.Bold);
            this.btnEdit.Appearance.ForeColor = Color.White;
            this.btnEdit.Appearance.Options.UseBackColor = true;
            this.btnEdit.Appearance.Options.UseFont = true;
            this.btnEdit.Appearance.Options.UseForeColor = true;
            this.btnEdit.Location = new Point(130, 15);
            this.btnEdit.Name = "btnEdit";
            this.btnEdit.Size = new Size(100, 30);
            this.btnEdit.TabIndex = 1;
            this.btnEdit.Text = "تعديل";
            this.btnEdit.Click += btnEdit_Click;

            // btnDelete
            this.btnDelete.Appearance.BackColor = Color.FromArgb(220, 53, 69);
            this.btnDelete.Appearance.Font = new Font("Segoe UI", 10F, FontStyle.Bold);
            this.btnDelete.Appearance.ForeColor = Color.White;
            this.btnDelete.Appearance.Options.UseBackColor = true;
            this.btnDelete.Appearance.Options.UseFont = true;
            this.btnDelete.Appearance.Options.UseForeColor = true;
            this.btnDelete.Location = new Point(240, 15);
            this.btnDelete.Name = "btnDelete";
            this.btnDelete.Size = new Size(100, 30);
            this.btnDelete.TabIndex = 2;
            this.btnDelete.Text = "حذف";
            this.btnDelete.Click += btnDelete_Click;

            // btnRefresh
            this.btnRefresh.Appearance.BackColor = Color.FromArgb(108, 117, 125);
            this.btnRefresh.Appearance.Font = new Font("Segoe UI", 10F, FontStyle.Bold);
            this.btnRefresh.Appearance.ForeColor = Color.White;
            this.btnRefresh.Appearance.Options.UseBackColor = true;
            this.btnRefresh.Appearance.Options.UseFont = true;
            this.btnRefresh.Appearance.Options.UseForeColor = true;
            this.btnRefresh.Location = new Point(350, 15);
            this.btnRefresh.Name = "btnRefresh";
            this.btnRefresh.Size = new Size(100, 30);
            this.btnRefresh.TabIndex = 3;
            this.btnRefresh.Text = "تحديث";
            this.btnRefresh.Click += btnRefresh_Click;

            // btnGridView
            this.btnGridView.Appearance.BackColor = Color.FromArgb(255, 193, 7);
            this.btnGridView.Appearance.Font = new Font("Segoe UI", 10F, FontStyle.Bold);
            this.btnGridView.Appearance.ForeColor = Color.Black;
            this.btnGridView.Appearance.Options.UseBackColor = true;
            this.btnGridView.Appearance.Options.UseFont = true;
            this.btnGridView.Appearance.Options.UseForeColor = true;
            this.btnGridView.Location = new Point(460, 15);
            this.btnGridView.Name = "btnGridView";
            this.btnGridView.Size = new Size(100, 30);
            this.btnGridView.TabIndex = 4;
            this.btnGridView.Text = "عرض الشبكة";
            this.btnGridView.Click += btnGridView_Click;

            // btnClose
            this.btnClose.Appearance.Font = new Font("Segoe UI", 10F);
            this.btnClose.Location = new Point(1080, 15);
            this.btnClose.Name = "btnClose";
            this.btnClose.Size = new Size(100, 30);
            this.btnClose.TabIndex = 5;
            this.btnClose.Text = "إغلاق";
            this.btnClose.Click += btnClose_Click;
        }
    }
}
