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
    /// نموذج إدارة العملاء - Customers Management Form
    /// </summary>
    public partial class CustomersForm : XtraForm, IDisposable
    {
        private ICustomerService _customerService = null!;
        private BindingList<Customer> _customers = null!;

        // DevExpress Controls
        private GridControl gridCustomers = null!;
        private GridView gridViewCustomers = null!;
        private SimpleButton btnAdd = null!;
        private SimpleButton btnEdit = null!;
        private SimpleButton btnDelete = null!;
        private SimpleButton btnRefresh = null!;
        private SimpleButton btnClose = null!;
        private TextEdit txtSearch = null!;
        private LabelControl lblSearch = null!;
        private PanelControl panelTop = null!;
        private PanelControl panelBottom = null!;

        public CustomersForm()
        {
            try
            {
                InitializeComponent();
                InitializeServices();
                SetupForm();

                // Load customers after form is fully initialized
                this.Load += CustomersForm_Load;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"خطأ في إنشاء نموذج العملاء: {ex}");
                XtraMessageBox.Show($"خطأ في تهيئة نموذج العملاء:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private async void CustomersForm_Load(object? sender, EventArgs e)
        {
            // Test database connection first
            if (await TestDatabaseConnectionAsync())
            {
                await LoadCustomersAsync();
            }
            else
            {
                XtraMessageBox.Show("فشل في الاتصال بقاعدة البيانات. يرجى التحقق من إعدادات الاتصال.", "خطأ في الاتصال",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private async Task<bool> TestDatabaseConnectionAsync()
        {
            try
            {
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

        private void InitializeServices()
        {
            try
            {
                // إنشاء الخدمات - Initialize services
                // Note: We'll create DbContext instances per operation to avoid threading issues
                _customerService = new CustomerService();

                // Log for debugging
                System.Diagnostics.Debug.WriteLine("تم تهيئة خدمات العملاء بنجاح");
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"خطأ في تهيئة خدمات العملاء: {ex}");
                throw new InvalidOperationException("فشل في تهيئة خدمات العملاء", ex);
            }
        }

        private void InitializeComponent()
        {
            this.gridCustomers = new DevExpress.XtraGrid.GridControl();
            this.gridViewCustomers = new DevExpress.XtraGrid.Views.Grid.GridView();
            this.btnAdd = new DevExpress.XtraEditors.SimpleButton();
            this.btnEdit = new DevExpress.XtraEditors.SimpleButton();
            this.btnDelete = new DevExpress.XtraEditors.SimpleButton();
            this.btnRefresh = new DevExpress.XtraEditors.SimpleButton();
            this.btnClose = new DevExpress.XtraEditors.SimpleButton();
            this.txtSearch = new DevExpress.XtraEditors.TextEdit();
            this.lblSearch = new DevExpress.XtraEditors.LabelControl();
            this.panelTop = new DevExpress.XtraEditors.PanelControl();
            this.panelBottom = new DevExpress.XtraEditors.PanelControl();

            ((System.ComponentModel.ISupportInitialize)(this.gridCustomers)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridViewCustomers)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtSearch.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelTop)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelBottom)).BeginInit();
            this.panelTop.SuspendLayout();
            this.panelBottom.SuspendLayout();
            this.SuspendLayout();

            // panelTop
            this.panelTop.Controls.Add(this.txtSearch);
            this.panelTop.Controls.Add(this.lblSearch);
            this.panelTop.Dock = System.Windows.Forms.DockStyle.Top;
            this.panelTop.Location = new System.Drawing.Point(0, 0);
            this.panelTop.Name = "panelTop";
            this.panelTop.Size = new System.Drawing.Size(1000, 60);
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

            // gridCustomers
            this.gridCustomers.Dock = System.Windows.Forms.DockStyle.Fill;
            this.gridCustomers.Location = new System.Drawing.Point(0, 60);
            this.gridCustomers.MainView = this.gridViewCustomers;
            this.gridCustomers.Name = "gridCustomers";
            this.gridCustomers.Size = new System.Drawing.Size(1000, 440);
            this.gridCustomers.TabIndex = 1;
            this.gridCustomers.ViewCollection.AddRange(new DevExpress.XtraGrid.Views.Base.BaseView[] { this.gridViewCustomers });

            // gridViewCustomers
            this.gridViewCustomers.GridControl = this.gridCustomers;
            this.gridViewCustomers.Name = "gridViewCustomers";
            this.gridViewCustomers.OptionsView.ShowGroupPanel = false;
            this.gridViewCustomers.OptionsView.ColumnAutoWidth = false;
            this.gridViewCustomers.OptionsSelection.EnableAppearanceFocusedCell = false;
            this.gridViewCustomers.OptionsSelection.MultiSelect = false;
            this.gridViewCustomers.FocusedRowChanged += new DevExpress.XtraGrid.Views.Base.FocusedRowChangedEventHandler(this.gridViewCustomers_FocusedRowChanged);
            this.gridViewCustomers.DoubleClick += new System.EventHandler(this.gridViewCustomers_DoubleClick);

            // panelBottom
            this.panelBottom.Controls.Add(this.btnClose);
            this.panelBottom.Controls.Add(this.btnRefresh);
            this.panelBottom.Controls.Add(this.btnDelete);
            this.panelBottom.Controls.Add(this.btnEdit);
            this.panelBottom.Controls.Add(this.btnAdd);
            this.panelBottom.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.panelBottom.Location = new System.Drawing.Point(0, 500);
            this.panelBottom.Name = "panelBottom";
            this.panelBottom.Size = new System.Drawing.Size(1000, 60);
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

            // btnClose
            this.btnClose.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F);
            this.btnClose.Location = new System.Drawing.Point(880, 15);
            this.btnClose.Name = "btnClose";
            this.btnClose.Size = new System.Drawing.Size(100, 30);
            this.btnClose.TabIndex = 4;
            this.btnClose.Text = "إغلاق";
            this.btnClose.Click += new System.EventHandler(this.btnClose_Click);

            // CustomersForm
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1000, 560);
            this.Controls.Add(this.gridCustomers);
            this.Controls.Add(this.panelTop);
            this.Controls.Add(this.panelBottom);
            this.IconOptions.ShowIcon = false;
            this.Name = "CustomersForm";
            this.RightToLeft = System.Windows.Forms.RightToLeft.Yes;
            this.RightToLeftLayout = true;
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "إدارة العملاء";
            this.WindowState = System.Windows.Forms.FormWindowState.Maximized;

            ((System.ComponentModel.ISupportInitialize)(this.gridCustomers)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridViewCustomers)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtSearch.Properties)).EndInit();
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
            this.Text = "إدارة العملاء - Customer Management";
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

            // تحديث حالة الأزرار - Update button states
            UpdateButtonStates();
        }

        private void SetupGrid()
        {
            // إعداد أعمدة الشبكة - Setup grid columns
            gridViewCustomers.Columns.Clear();

            var idColumn = gridViewCustomers.Columns.Add();
            idColumn.FieldName = "Id";
            idColumn.Caption = "الرقم";
            idColumn.Visible = false;

            var nameColumn = gridViewCustomers.Columns.Add();
            nameColumn.FieldName = "CustomerName";
            nameColumn.Caption = "اسم العميل";
            nameColumn.Width = 200;
            nameColumn.VisibleIndex = 0;

            var companyColumn = gridViewCustomers.Columns.Add();
            companyColumn.FieldName = "CompanyName";
            companyColumn.Caption = "اسم الشركة";
            companyColumn.Width = 150;
            companyColumn.VisibleIndex = 1;

            var phoneColumn = gridViewCustomers.Columns.Add();
            phoneColumn.FieldName = "Phone";
            phoneColumn.Caption = "الهاتف";
            phoneColumn.Width = 120;
            phoneColumn.VisibleIndex = 2;

            var mobileColumn = gridViewCustomers.Columns.Add();
            mobileColumn.FieldName = "Mobile";
            mobileColumn.Caption = "الجوال";
            mobileColumn.Width = 120;
            mobileColumn.VisibleIndex = 3;

            var emailColumn = gridViewCustomers.Columns.Add();
            emailColumn.FieldName = "Email";
            emailColumn.Caption = "البريد الإلكتروني";
            emailColumn.Width = 180;
            emailColumn.VisibleIndex = 4;

            var cityColumn = gridViewCustomers.Columns.Add();
            cityColumn.FieldName = "City";
            cityColumn.Caption = "المدينة";
            cityColumn.Width = 100;
            cityColumn.VisibleIndex = 5;

            var balanceColumn = gridViewCustomers.Columns.Add();
            balanceColumn.FieldName = "CurrentBalance";
            balanceColumn.Caption = "الرصيد الحالي";
            balanceColumn.Width = 120;
            balanceColumn.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            balanceColumn.DisplayFormat.FormatString = "N2";
            balanceColumn.VisibleIndex = 6;

            var creditColumn = gridViewCustomers.Columns.Add();
            creditColumn.FieldName = "CreditLimit";
            creditColumn.Caption = "حد الائتمان";
            creditColumn.Width = 120;
            creditColumn.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            creditColumn.DisplayFormat.FormatString = "N2";
            creditColumn.VisibleIndex = 7;

            var activeColumn = gridViewCustomers.Columns.Add();
            activeColumn.FieldName = "IsActive";
            activeColumn.Caption = "نشط";
            activeColumn.Width = 80;
            activeColumn.VisibleIndex = 8;

            // إعداد خصائص الشبكة - Setup grid properties
            gridViewCustomers.OptionsView.ShowAutoFilterRow = true;
            gridViewCustomers.OptionsSelection.EnableAppearanceFocusedCell = false;
            gridViewCustomers.OptionsSelection.MultiSelect = false;
            gridViewCustomers.OptionsView.ColumnAutoWidth = false;
            gridViewCustomers.OptionsView.ShowGroupPanel = false;
            gridViewCustomers.OptionsView.ShowIndicator = true;
            gridViewCustomers.OptionsNavigation.AutoFocusNewRow = true;
            gridViewCustomers.OptionsNavigation.EnterMoveNextColumn = true;

            // RTL support
            gridViewCustomers.Appearance.HeaderPanel.TextOptions.HAlignment = DevExpress.Utils.HorzAlignment.Center;
            gridViewCustomers.Appearance.Row.TextOptions.HAlignment = DevExpress.Utils.HorzAlignment.Near;

            // Event handlers
            gridViewCustomers.FocusedRowChanged += gridViewCustomers_FocusedRowChanged;
            gridViewCustomers.DoubleClick += gridViewCustomers_DoubleClick;

            gridViewCustomers.BestFitColumns();
        }

        private async Task LoadCustomersAsync()
        {
            try
            {
                var customers = await _customerService.GetAllCustomersAsync();
                _customers = new BindingList<Customer>(customers.ToList());

                // Ensure proper data binding
                gridCustomers.DataSource = null;
                gridCustomers.DataSource = _customers;
                gridCustomers.RefreshDataSource();

                // Force grid view refresh
                gridViewCustomers.RefreshData();
                gridViewCustomers.BestFitColumns();

                UpdateButtonStates();

                // Log for debugging
                System.Diagnostics.Debug.WriteLine($"تم تحميل {_customers.Count} عميل");
            }
            catch (Exception ex)
            {
                LoadingHelper.ShowError($"خطأ في تحميل بيانات العملاء:\n{ex.Message}\n\nتفاصيل الخطأ:\n{ex.InnerException?.Message}");

                // Log the full exception for debugging
                System.Diagnostics.Debug.WriteLine($"خطأ في تحميل العملاء: {ex}");
                throw; // Re-throw to be handled by LoadingHelper
            }
        }

        private void UpdateButtonStates()
        {
            var hasSelection = gridViewCustomers.FocusedRowHandle >= 0;
            btnEdit.Enabled = hasSelection;
            btnDelete.Enabled = hasSelection;
        }

        private Customer? GetSelectedCustomer()
        {
            if (gridViewCustomers.FocusedRowHandle >= 0)
                return gridViewCustomers.GetRow(gridViewCustomers.FocusedRowHandle) as Customer;
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
                    await LoadCustomersAsync();
                    return;
                }

                this.Cursor = Cursors.WaitCursor;

                var customers = await _customerService.SearchCustomersAsync(searchTerm);
                _customers = new BindingList<Customer>(customers.ToList());

                // Ensure proper data binding
                gridCustomers.DataSource = null;
                gridCustomers.DataSource = _customers;
                gridCustomers.RefreshDataSource();

                // Force grid view refresh
                gridViewCustomers.RefreshData();

                UpdateButtonStates();

                // Log for debugging
                System.Diagnostics.Debug.WriteLine($"تم العثور على {_customers.Count} عميل للبحث: {searchTerm}");
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في البحث:\n{ex.Message}\n\nتفاصيل الخطأ:\n{ex.InnerException?.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);

                // Log the full exception for debugging
                System.Diagnostics.Debug.WriteLine($"خطأ في البحث: {ex}");
            }
            finally
            {
                this.Cursor = Cursors.Default;
            }
        }

        private void gridViewCustomers_FocusedRowChanged(object sender, DevExpress.XtraGrid.Views.Base.FocusedRowChangedEventArgs e)
        {
            UpdateButtonStates();
        }

        private void gridViewCustomers_DoubleClick(object? sender, EventArgs e)
        {
            if (btnEdit.Enabled)
                btnEdit_Click(sender, e);
        }

        private async void btnAdd_Click(object? sender, EventArgs e)
        {
            try
            {
                var addForm = new CustomerAddEditForm();
                if (addForm.ShowDialog() == DialogResult.OK)
                {
                    await LoadCustomersAsync();
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
                var selectedCustomer = GetSelectedCustomer();
                if (selectedCustomer == null)
                {
                    XtraMessageBox.Show("يرجى اختيار عميل للتعديل", "تنبيه",
                        MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                var editForm = new CustomerAddEditForm(selectedCustomer);
                if (editForm.ShowDialog() == DialogResult.OK)
                {
                    await LoadCustomersAsync();
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
                var selectedCustomer = GetSelectedCustomer();
                if (selectedCustomer == null)
                {
                    XtraMessageBox.Show("يرجى اختيار عميل للحذف", "تنبيه",
                        MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                var result = XtraMessageBox.Show(
                    $"هل أنت متأكد من حذف العميل '{selectedCustomer.CustomerName}'؟\nهذا الإجراء لا يمكن التراجع عنه.",
                    "تأكيد الحذف", MessageBoxButtons.YesNo, MessageBoxIcon.Question);

                if (result == DialogResult.Yes)
                {
                    this.Cursor = Cursors.WaitCursor;

                    var success = await _customerService.DeleteCustomerAsync(selectedCustomer.Id);
                    if (success)
                    {
                        XtraMessageBox.Show("تم حذف العميل بنجاح", "نجح",
                            MessageBoxButtons.OK, MessageBoxIcon.Information);
                        await LoadCustomersAsync();
                    }
                    else
                    {
                        XtraMessageBox.Show("فشل في حذف العميل", "خطأ",
                            MessageBoxButtons.OK, MessageBoxIcon.Error);
                    }
                }
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في حذف العميل:\n{ex.Message}", "خطأ",
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
                await LoadCustomersAsync();
                LoadingHelper.ShowSuccess("تم تحديث بيانات العملاء بنجاح", "تحديث");
            }, "جاري تحديث بيانات العملاء...", this);
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
                if (_customerService is IDisposable disposableService)
                {
                    disposableService.Dispose();
                }
            }
            base.Dispose(disposing);
        }
    }
}


