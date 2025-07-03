using DXApplication1.Models;
using DXApplication1.BusinessLogicLayer;
using DevExpress.XtraEditors;
using DevExpress.XtraLayout;
using System.ComponentModel;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// نموذج إضافة وتعديل المنتجات - Product Add/Edit Form
    /// </summary>
    public partial class ProductAddEditForm : XtraForm
    {
        private readonly IProductService _productService;
        private readonly IProductValidationService _validationService;
        private readonly Product? _product;
        private readonly bool _isEditMode;
        private BindingList<Category> _categories = null!;

        // DevExpress Controls
        private LayoutControl layoutControl1 = null!;
        private LayoutControlGroup layoutControlGroup1 = null!;
        
        // Basic Information Controls
        private TextEdit txtProductName = null!;
        private TextEdit txtProductCode = null!;
        private TextEdit txtBarcode = null!;
        private MemoEdit txtDescription = null!;
        private LookUpEdit cmbCategory = null!;
        
        // Pricing Controls
        private SpinEdit spnPurchasePrice = null!;
        private SpinEdit spnSalePrice = null!;
        private SpinEdit spnMinimumPrice = null!;
        private SpinEdit spnTaxRate = null!;
        
        // Inventory Controls
        private SpinEdit spnStockQuantity = null!;
        private SpinEdit spnMinimumStock = null!;
        private SpinEdit spnMaximumStock = null!;
        private TextEdit txtUnit = null!;
        private CheckEdit chkTrackInventory = null!;
        private CheckEdit chkAllowNegativeStock = null!;
        
        // Additional Controls
        private ButtonEdit btnImagePath = null!;
        private MemoEdit txtNotes = null!;
        private CheckEdit chkIsActive = null!;
        
        // Action Buttons
        private SimpleButton btnSave = null!;
        private SimpleButton btnCancel = null!;

        public ProductAddEditForm(IProductService productService, Product? product = null)
        {
            _productService = productService ?? throw new ArgumentNullException(nameof(productService));
            _validationService = new ProductValidationService();
            _product = product;
            _isEditMode = product != null;

            InitializeComponent();
            SetupForm();
            _ = LoadDataAsync();
        }

        private void SetupForm()
        {
            // إعداد النموذج - Setup form
            this.Text = _isEditMode ? "تعديل المنتج - Edit Product" : "إضافة منتج جديد - Add New Product";
            this.StartPosition = FormStartPosition.CenterParent;
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Size = new Size(800, 700);

            // RTL support
            this.RightToLeft = RightToLeft.Yes;
            this.RightToLeftLayout = true;

            // Modern UI styling
            this.LookAndFeel.UseDefaultLookAndFeel = false;
            this.LookAndFeel.SkinName = "WXI";

            // Set Arabic font
            this.Font = new Font("Segoe UI", 9.5F, FontStyle.Regular);
        }

        private async Task LoadDataAsync()
        {
            try
            {
                // تحميل الفئات - Load categories
                await LoadCategoriesAsync();

                // تحميل بيانات المنتج في حالة التعديل - Load product data in edit mode
                if (_isEditMode && _product != null)
                {
                    LoadProductData();
                }
                else
                {
                    // تعيين القيم الافتراضية - Set default values
                    SetDefaultValues();
                }
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
                
                cmbCategory.Properties.DataSource = _categories;
                cmbCategory.Properties.DisplayMember = "CategoryName";
                cmbCategory.Properties.ValueMember = "Id";
                cmbCategory.Properties.Columns.Clear();
                cmbCategory.Properties.Columns.Add(new DevExpress.XtraEditors.Controls.LookUpColumnInfo("CategoryName", "اسم الفئة"));
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في تحميل الفئات:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void LoadProductData()
        {
            if (_product == null) return;

            txtProductName.Text = _product.ProductName;
            txtProductCode.Text = _product.ProductCode ?? "";
            txtBarcode.Text = _product.Barcode ?? "";
            txtDescription.Text = _product.Description ?? "";
            cmbCategory.EditValue = _product.CategoryId;
            
            spnPurchasePrice.Value = _product.PurchasePrice;
            spnSalePrice.Value = _product.SalePrice;
            spnMinimumPrice.Value = _product.MinimumPrice;
            spnTaxRate.Value = _product.TaxRate;
            
            spnStockQuantity.Value = _product.StockQuantity;
            spnMinimumStock.Value = _product.MinimumStock;
            spnMaximumStock.Value = _product.MaximumStock;
            txtUnit.Text = _product.Unit ?? "قطعة";
            chkTrackInventory.Checked = _product.TrackInventory;
            chkAllowNegativeStock.Checked = _product.AllowNegativeStock;
            
            btnImagePath.Text = _product.ImagePath ?? "";
            txtNotes.Text = _product.Notes ?? "";
            chkIsActive.Checked = _product.IsActive;
        }

        private void SetDefaultValues()
        {
            txtUnit.Text = "قطعة";
            chkTrackInventory.Checked = true;
            chkAllowNegativeStock.Checked = false;
            chkIsActive.Checked = true;
            spnTaxRate.Value = 15; // Default VAT rate in Saudi Arabia
        }

        private async Task<bool> ValidateFormAsync()
        {
            try
            {
                // إنشاء منتج من البيانات المدخلة - Create product from form data
                var product = CreateProductFromForm();

                // التحقق من صحة البيانات باستخدام خدمة التحقق - Validate using validation service
                var validationResult = await _validationService.ValidateProductAsync(product, _isEditMode);

                // عرض الأخطاء إن وجدت - Display errors if any
                if (!validationResult.IsValid)
                {
                    var errorMessage = "يرجى تصحيح الأخطاء التالية:\n\n" + validationResult.GetErrorsAsString();
                    XtraMessageBox.Show(errorMessage, "أخطاء في البيانات",
                        MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return false;
                }

                // عرض التحذيرات إن وجدت - Display warnings if any
                if (validationResult.Warnings.Any())
                {
                    var warningMessage = "تحذيرات:\n\n" + validationResult.GetWarningsAsString() +
                                       "\n\nهل تريد المتابعة؟";
                    var result = XtraMessageBox.Show(warningMessage, "تحذيرات",
                        MessageBoxButtons.YesNo, MessageBoxIcon.Question);

                    if (result != DialogResult.Yes)
                        return false;
                }

                return true;
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في التحقق من صحة البيانات:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                return false;
            }
        }

        private Product CreateProductFromForm()
        {
            var product = new Product
            {
                ProductName = txtProductName.Text.Trim(),
                ProductCode = string.IsNullOrWhiteSpace(txtProductCode.Text) ? null : txtProductCode.Text.Trim(),
                Barcode = string.IsNullOrWhiteSpace(txtBarcode.Text) ? null : txtBarcode.Text.Trim(),
                Description = string.IsNullOrWhiteSpace(txtDescription.Text) ? null : txtDescription.Text.Trim(),
                CategoryId = Convert.ToInt32(cmbCategory.EditValue),
                
                PurchasePrice = spnPurchasePrice.Value,
                SalePrice = spnSalePrice.Value,
                MinimumPrice = spnMinimumPrice.Value,
                TaxRate = spnTaxRate.Value,
                
                StockQuantity = Convert.ToInt32(spnStockQuantity.Value),
                MinimumStock = Convert.ToInt32(spnMinimumStock.Value),
                MaximumStock = Convert.ToInt32(spnMaximumStock.Value),
                Unit = txtUnit.Text.Trim(),
                TrackInventory = chkTrackInventory.Checked,
                AllowNegativeStock = chkAllowNegativeStock.Checked,
                
                ImagePath = string.IsNullOrWhiteSpace(btnImagePath.Text) ? null : btnImagePath.Text.Trim(),
                Notes = string.IsNullOrWhiteSpace(txtNotes.Text) ? null : txtNotes.Text.Trim(),
                IsActive = chkIsActive.Checked
            };

            return product;
        }

        private async void btnSave_Click(object? sender, EventArgs e)
        {
            try
            {
                if (!await ValidateFormAsync())
                    return;

                this.Cursor = Cursors.WaitCursor;
                btnSave.Enabled = false;

                var product = CreateProductFromForm();
                bool success;

                if (_isEditMode)
                {
                    product.Id = _product!.Id;
                    success = await _productService.UpdateProductAsync(product);
                }
                else
                {
                    success = await _productService.CreateProductAsync(product);
                }

                if (success)
                {
                    XtraMessageBox.Show(
                        _isEditMode ? "تم تحديث المنتج بنجاح" : "تم إضافة المنتج بنجاح",
                        "نجح", MessageBoxButtons.OK, MessageBoxIcon.Information);

                    this.DialogResult = DialogResult.OK;
                    this.Close();
                }
                else
                {
                    XtraMessageBox.Show(
                        _isEditMode ? "فشل في تحديث المنتج" : "فشل في إضافة المنتج",
                        "خطأ", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في حفظ المنتج:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                this.Cursor = Cursors.Default;
                btnSave.Enabled = true;
            }
        }

        private void btnCancel_Click(object? sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
            this.Close();
        }

        private void btnImagePath_ButtonClick(object? sender, DevExpress.XtraEditors.Controls.ButtonPressedEventArgs e)
        {
            using var openFileDialog = new OpenFileDialog
            {
                Title = "اختيار صورة المنتج",
                Filter = "Image Files|*.jpg;*.jpeg;*.png;*.bmp;*.gif|All Files|*.*",
                FilterIndex = 1
            };

            if (openFileDialog.ShowDialog() == DialogResult.OK)
            {
                btnImagePath.Text = openFileDialog.FileName;
            }
        }

        private void InitializeComponent()
        {
            this.layoutControl1 = new LayoutControl();
            this.layoutControlGroup1 = new LayoutControlGroup();

            // Initialize all controls
            this.txtProductName = new TextEdit();
            this.txtProductCode = new TextEdit();
            this.txtBarcode = new TextEdit();
            this.txtDescription = new MemoEdit();
            this.cmbCategory = new LookUpEdit();

            this.spnPurchasePrice = new SpinEdit();
            this.spnSalePrice = new SpinEdit();
            this.spnMinimumPrice = new SpinEdit();
            this.spnTaxRate = new SpinEdit();

            this.spnStockQuantity = new SpinEdit();
            this.spnMinimumStock = new SpinEdit();
            this.spnMaximumStock = new SpinEdit();
            this.txtUnit = new TextEdit();
            this.chkTrackInventory = new CheckEdit();
            this.chkAllowNegativeStock = new CheckEdit();

            this.btnImagePath = new ButtonEdit();
            this.txtNotes = new MemoEdit();
            this.chkIsActive = new CheckEdit();

            this.btnSave = new SimpleButton();
            this.btnCancel = new SimpleButton();

            ((System.ComponentModel.ISupportInitialize)(this.layoutControl1)).BeginInit();
            this.layoutControl1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.layoutControlGroup1)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtProductName.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtProductCode.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtBarcode.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtDescription.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.cmbCategory.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnPurchasePrice.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnSalePrice.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnMinimumPrice.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnTaxRate.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnStockQuantity.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnMinimumStock.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnMaximumStock.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtUnit.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.chkTrackInventory.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.chkAllowNegativeStock.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.btnImagePath.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtNotes.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.chkIsActive.Properties)).BeginInit();
            this.SuspendLayout();

            // layoutControl1
            this.layoutControl1.Controls.Add(this.txtProductName);
            this.layoutControl1.Controls.Add(this.txtProductCode);
            this.layoutControl1.Controls.Add(this.txtBarcode);
            this.layoutControl1.Controls.Add(this.txtDescription);
            this.layoutControl1.Controls.Add(this.cmbCategory);
            this.layoutControl1.Controls.Add(this.spnPurchasePrice);
            this.layoutControl1.Controls.Add(this.spnSalePrice);
            this.layoutControl1.Controls.Add(this.spnMinimumPrice);
            this.layoutControl1.Controls.Add(this.spnTaxRate);
            this.layoutControl1.Controls.Add(this.spnStockQuantity);
            this.layoutControl1.Controls.Add(this.spnMinimumStock);
            this.layoutControl1.Controls.Add(this.spnMaximumStock);
            this.layoutControl1.Controls.Add(this.txtUnit);
            this.layoutControl1.Controls.Add(this.chkTrackInventory);
            this.layoutControl1.Controls.Add(this.chkAllowNegativeStock);
            this.layoutControl1.Controls.Add(this.btnImagePath);
            this.layoutControl1.Controls.Add(this.txtNotes);
            this.layoutControl1.Controls.Add(this.chkIsActive);
            this.layoutControl1.Controls.Add(this.btnSave);
            this.layoutControl1.Controls.Add(this.btnCancel);
            this.layoutControl1.Dock = DockStyle.Fill;
            this.layoutControl1.Location = new Point(0, 0);
            this.layoutControl1.Name = "layoutControl1";
            this.layoutControl1.Root = this.layoutControlGroup1;
            this.layoutControl1.Size = new Size(800, 700);
            this.layoutControl1.TabIndex = 0;
            this.layoutControl1.Text = "layoutControl1";

            // Configure controls properties
            ConfigureControlProperties();

            // Create layout structure
            CreateLayoutStructure();

            // ProductAddEditForm
            this.AutoScaleDimensions = new SizeF(6F, 13F);
            this.AutoScaleMode = AutoScaleMode.Font;
            this.ClientSize = new Size(800, 700);
            this.Controls.Add(this.layoutControl1);
            this.Name = "ProductAddEditForm";
            this.Text = "إدارة المنتج";

            ((System.ComponentModel.ISupportInitialize)(this.layoutControl1)).EndInit();
            this.layoutControl1.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.layoutControlGroup1)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtProductName.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtProductCode.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtBarcode.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtDescription.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.cmbCategory.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnPurchasePrice.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnSalePrice.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnMinimumPrice.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnTaxRate.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnStockQuantity.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnMinimumStock.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.spnMaximumStock.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtUnit.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.chkTrackInventory.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.chkAllowNegativeStock.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.btnImagePath.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtNotes.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.chkIsActive.Properties)).EndInit();
            this.ResumeLayout(false);
        }

        private void ConfigureControlProperties()
        {
            // Configure text controls
            txtProductName.Properties.MaxLength = 200;
            txtProductCode.Properties.MaxLength = 100;
            txtBarcode.Properties.MaxLength = 100;
            txtDescription.Properties.MaxLength = 500;
            txtUnit.Properties.MaxLength = 50;
            txtNotes.Properties.MaxLength = 1000;

            // Configure spin controls
            spnPurchasePrice.Properties.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnPurchasePrice.Properties.DisplayFormat.FormatString = "N2";
            spnPurchasePrice.Properties.EditFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnPurchasePrice.Properties.EditFormat.FormatString = "N2";
            spnPurchasePrice.Properties.MinValue = 0;
            spnPurchasePrice.Properties.MaxValue = 999999999;

            spnSalePrice.Properties.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnSalePrice.Properties.DisplayFormat.FormatString = "N2";
            spnSalePrice.Properties.EditFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnSalePrice.Properties.EditFormat.FormatString = "N2";
            spnSalePrice.Properties.MinValue = 0;
            spnSalePrice.Properties.MaxValue = 999999999;

            spnMinimumPrice.Properties.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnMinimumPrice.Properties.DisplayFormat.FormatString = "N2";
            spnMinimumPrice.Properties.EditFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnMinimumPrice.Properties.EditFormat.FormatString = "N2";
            spnMinimumPrice.Properties.MinValue = 0;
            spnMinimumPrice.Properties.MaxValue = 999999999;

            spnTaxRate.Properties.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnTaxRate.Properties.DisplayFormat.FormatString = "N2";
            spnTaxRate.Properties.EditFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnTaxRate.Properties.EditFormat.FormatString = "N2";
            spnTaxRate.Properties.MinValue = 0;
            spnTaxRate.Properties.MaxValue = 100;

            spnStockQuantity.Properties.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnStockQuantity.Properties.DisplayFormat.FormatString = "N0";
            spnStockQuantity.Properties.EditFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnStockQuantity.Properties.EditFormat.FormatString = "N0";
            spnStockQuantity.Properties.MinValue = -999999;
            spnStockQuantity.Properties.MaxValue = 999999999;

            spnMinimumStock.Properties.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnMinimumStock.Properties.DisplayFormat.FormatString = "N0";
            spnMinimumStock.Properties.EditFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnMinimumStock.Properties.EditFormat.FormatString = "N0";
            spnMinimumStock.Properties.MinValue = 0;
            spnMinimumStock.Properties.MaxValue = 999999999;

            spnMaximumStock.Properties.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnMaximumStock.Properties.DisplayFormat.FormatString = "N0";
            spnMaximumStock.Properties.EditFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnMaximumStock.Properties.EditFormat.FormatString = "N0";
            spnMaximumStock.Properties.MinValue = 0;
            spnMaximumStock.Properties.MaxValue = 999999999;

            // Configure button edit
            btnImagePath.Properties.Buttons.Clear();
            btnImagePath.Properties.Buttons.Add(new DevExpress.XtraEditors.Controls.EditorButton(DevExpress.XtraEditors.Controls.ButtonPredefines.Ellipsis));
            btnImagePath.ButtonClick += btnImagePath_ButtonClick;

            // Configure buttons
            btnSave.Text = "حفظ";
            btnSave.Appearance.BackColor = Color.FromArgb(0, 122, 204);
            btnSave.Appearance.ForeColor = Color.White;
            btnSave.Appearance.Options.UseBackColor = true;
            btnSave.Appearance.Options.UseForeColor = true;
            btnSave.Click += btnSave_Click;

            btnCancel.Text = "إلغاء";
            btnCancel.Click += btnCancel_Click;

            // Configure checkboxes
            chkTrackInventory.Text = "تتبع المخزون";
            chkAllowNegativeStock.Text = "السماح بالمخزون السالب";
            chkIsActive.Text = "نشط";
        }

        private void CreateLayoutStructure()
        {
            // Create layout groups
            var basicInfoGroup = new LayoutControlGroup
            {
                Text = "المعلومات الأساسية",
                GroupBordersVisible = true
            };

            var pricingGroup = new LayoutControlGroup
            {
                Text = "الأسعار والضرائب",
                GroupBordersVisible = true
            };

            var inventoryGroup = new LayoutControlGroup
            {
                Text = "إدارة المخزون",
                GroupBordersVisible = true
            };

            var additionalGroup = new LayoutControlGroup
            {
                Text = "معلومات إضافية",
                GroupBordersVisible = true
            };

            var buttonGroup = new LayoutControlGroup
            {
                GroupBordersVisible = false
            };

            // Add layout items to basic info group
            var item1 = new LayoutControlItem { Control = txtProductName, Text = "اسم المنتج *:" };
            var item2 = new LayoutControlItem { Control = txtProductCode, Text = "كود المنتج:" };
            var item3 = new LayoutControlItem { Control = txtBarcode, Text = "الباركود:" };
            var item4 = new LayoutControlItem { Control = cmbCategory, Text = "الفئة *:" };
            var item5 = new LayoutControlItem { Control = txtDescription, Text = "الوصف:" };
            item5.Size = new Size(item5.Size.Width, 60);

            basicInfoGroup.AddItem(item1);
            basicInfoGroup.AddItem(item2);
            basicInfoGroup.AddItem(item3);
            basicInfoGroup.AddItem(item4);
            basicInfoGroup.AddItem(item5);

            // Add layout items to pricing group
            var priceItem1 = new LayoutControlItem { Control = spnPurchasePrice, Text = "سعر الشراء:" };
            var priceItem2 = new LayoutControlItem { Control = spnSalePrice, Text = "سعر البيع *:" };
            var priceItem3 = new LayoutControlItem { Control = spnMinimumPrice, Text = "الحد الأدنى للسعر:" };
            var priceItem4 = new LayoutControlItem { Control = spnTaxRate, Text = "معدل الضريبة (%):" };

            pricingGroup.AddItem(priceItem1);
            pricingGroup.AddItem(priceItem2);
            pricingGroup.AddItem(priceItem3);
            pricingGroup.AddItem(priceItem4);

            // Add layout items to inventory group
            var invItem1 = new LayoutControlItem { Control = spnStockQuantity, Text = "الكمية الحالية:" };
            var invItem2 = new LayoutControlItem { Control = spnMinimumStock, Text = "الحد الأدنى للمخزون:" };
            var invItem3 = new LayoutControlItem { Control = spnMaximumStock, Text = "الحد الأقصى للمخزون:" };
            var invItem4 = new LayoutControlItem { Control = txtUnit, Text = "الوحدة:" };
            var invItem5 = new LayoutControlItem { Control = chkTrackInventory, TextVisible = false };
            var invItem6 = new LayoutControlItem { Control = chkAllowNegativeStock, TextVisible = false };

            inventoryGroup.AddItem(invItem1);
            inventoryGroup.AddItem(invItem2);
            inventoryGroup.AddItem(invItem3);
            inventoryGroup.AddItem(invItem4);
            inventoryGroup.AddItem(invItem5);
            inventoryGroup.AddItem(invItem6);

            // Add layout items to additional group
            var addItem1 = new LayoutControlItem { Control = btnImagePath, Text = "مسار الصورة:" };
            var addItem2 = new LayoutControlItem { Control = txtNotes, Text = "ملاحظات:" };
            addItem2.Size = new Size(addItem2.Size.Width, 60);

            additionalGroup.AddItem(addItem1);
            additionalGroup.AddItem(addItem2);
            var addItem3 = new LayoutControlItem { Control = chkIsActive, TextVisible = false };
            additionalGroup.AddItem(addItem3);

            // Add buttons
            var buttonItem1 = new LayoutControlItem { Control = btnSave, TextVisible = false };
            var buttonItem2 = new LayoutControlItem { Control = btnCancel, TextVisible = false };
            buttonGroup.AddItem(buttonItem1);
            buttonGroup.AddItem(buttonItem2);

            // Add groups to main layout
            layoutControlGroup1.AddItem(basicInfoGroup);
            layoutControlGroup1.AddItem(pricingGroup);
            layoutControlGroup1.AddItem(inventoryGroup);
            layoutControlGroup1.AddItem(additionalGroup);
            layoutControlGroup1.AddItem(buttonGroup);
        }
    }
}
