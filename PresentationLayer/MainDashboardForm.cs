using DXApplication1.Utilities;
using DXApplication1.Models;
using DXApplication1.BusinessLogicLayer;
using DXApplication1.DataAccessLayer;
using System.Drawing;
using System.Windows.Forms;
using DevExpress.XtraEditors;
using DevExpress.XtraNavBar;
using DevExpress.XtraCharts;
using DevExpress.XtraBars.Docking;
using DevExpress.XtraBars.Ribbon;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// نموذج لوحة التحكم الرئيسية - Main Dashboard Form
    /// </summary>
    public partial class MainDashboardForm : RibbonForm
    {
        private User? _currentUser;
        private IDashboardService _dashboardService = null!;
        private DevExpress.XtraBars.Ribbon.RibbonControl ribbonControl1 = null!;
        private DevExpress.XtraBars.BarButtonItem btnCustomers = null!;
        private DevExpress.XtraBars.BarButtonItem btnProducts = null!;
        private DevExpress.XtraBars.BarButtonItem btnInvoices = null!;
        private DevExpress.XtraBars.BarButtonItem btnReports = null!;
        private DevExpress.XtraBars.BarButtonItem btnUsers = null!;
        private DevExpress.XtraBars.BarButtonItem btnSettings = null!;
        private DevExpress.XtraBars.BarButtonItem btnLogout = null!;
        private DevExpress.XtraBars.BarButtonItem btnRefresh = null!;
        private DevExpress.XtraEditors.PanelControl panelControl1 = null!;
        private DevExpress.XtraCharts.ChartControl chartControl1 = null!;
        private DevExpress.XtraEditors.PanelControl panelControl2 = null!;
        private DevExpress.XtraEditors.LabelControl lblTotalSales = null!;
        private DevExpress.XtraEditors.LabelControl lblTotalCustomers = null!;
        private DevExpress.XtraEditors.LabelControl lblTotalProducts = null!;
        private DevExpress.XtraEditors.LabelControl lblTotalInvoices = null!;
        private DevExpress.XtraEditors.PanelControl panelRecentActivities = null!;
        private DevExpress.XtraEditors.LabelControl lblRecentActivitiesTitle = null!;
        private DevExpress.XtraGrid.GridControl gridRecentInvoices = null!;
        private DevExpress.XtraEditors.TileControl tileControl1 = null!;

        public MainDashboardForm(User currentUser)
        {
            InitializeComponent();
            _currentUser = currentUser;

            // إنشاء خدمة لوحة التحكم - Initialize dashboard service
            // Note: We'll create DbContext instances per operation to avoid threading issues
            _dashboardService = new DashboardService();

            SetupForm();
            SetupButtons();
            SetupTiles();
            _ = LoadDashboardDataAsync();
        }

        private void InitializeComponent()
        {
            this.ribbonControl1 = new DevExpress.XtraBars.Ribbon.RibbonControl();
            this.btnCustomers = new DevExpress.XtraBars.BarButtonItem();
            this.btnProducts = new DevExpress.XtraBars.BarButtonItem();
            this.btnInvoices = new DevExpress.XtraBars.BarButtonItem();
            this.btnReports = new DevExpress.XtraBars.BarButtonItem();
            this.btnUsers = new DevExpress.XtraBars.BarButtonItem();
            this.btnSettings = new DevExpress.XtraBars.BarButtonItem();
            this.btnLogout = new DevExpress.XtraBars.BarButtonItem();
            this.btnRefresh = new DevExpress.XtraBars.BarButtonItem();
            this.panelControl1 = new DevExpress.XtraEditors.PanelControl();
            this.chartControl1 = new DevExpress.XtraCharts.ChartControl();
            this.panelControl2 = new DevExpress.XtraEditors.PanelControl();
            this.lblTotalSales = new DevExpress.XtraEditors.LabelControl();
            this.lblTotalCustomers = new DevExpress.XtraEditors.LabelControl();
            this.lblTotalProducts = new DevExpress.XtraEditors.LabelControl();
            this.lblTotalInvoices = new DevExpress.XtraEditors.LabelControl();
            this.panelRecentActivities = new DevExpress.XtraEditors.PanelControl();
            this.lblRecentActivitiesTitle = new DevExpress.XtraEditors.LabelControl();
            this.gridRecentInvoices = new DevExpress.XtraGrid.GridControl();
            this.tileControl1 = new DevExpress.XtraEditors.TileControl();

            var ribbonPage1 = new DevExpress.XtraBars.Ribbon.RibbonPage();
            var ribbonPageGroup1 = new DevExpress.XtraBars.Ribbon.RibbonPageGroup();
            var ribbonPageGroup2 = new DevExpress.XtraBars.Ribbon.RibbonPageGroup();
            var ribbonPageGroup3 = new DevExpress.XtraBars.Ribbon.RibbonPageGroup();

            ((System.ComponentModel.ISupportInitialize)(this.ribbonControl1)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelControl1)).BeginInit();
            this.panelControl1.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.chartControl1)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelControl2)).BeginInit();
            this.panelControl2.SuspendLayout();
            this.SuspendLayout();

            // ribbonControl1
            this.ribbonControl1.ExpandCollapseItem.Id = 0;
            this.ribbonControl1.Items.AddRange(new DevExpress.XtraBars.BarItem[] {
                this.ribbonControl1.ExpandCollapseItem,
                this.btnCustomers,
                this.btnProducts,
                this.btnInvoices,
                this.btnReports,
                this.btnUsers,
                this.btnSettings,
                this.btnLogout,
                this.btnRefresh});
            this.ribbonControl1.Location = new System.Drawing.Point(0, 0);
            this.ribbonControl1.MaxItemId = 9;
            this.ribbonControl1.Name = "ribbonControl1";
            this.ribbonControl1.Pages.AddRange(new DevExpress.XtraBars.Ribbon.RibbonPage[] { ribbonPage1 });
            this.ribbonControl1.RightToLeft = System.Windows.Forms.RightToLeft.Yes;
            this.ribbonControl1.Size = new System.Drawing.Size(1200, 158);

            // Ribbon buttons
            this.btnCustomers.Caption = "العملاء";
            this.btnCustomers.Id = 1;
            this.btnCustomers.Name = "btnCustomers";
            this.btnCustomers.ItemClick += new DevExpress.XtraBars.ItemClickEventHandler(this.btnCustomers_Click);

            this.btnProducts.Caption = "المنتجات";
            this.btnProducts.Id = 2;
            this.btnProducts.Name = "btnProducts";
            this.btnProducts.ItemClick += new DevExpress.XtraBars.ItemClickEventHandler(this.btnProducts_Click);

            this.btnInvoices.Caption = "الفواتير";
            this.btnInvoices.Id = 3;
            this.btnInvoices.Name = "btnInvoices";
            this.btnInvoices.ItemClick += new DevExpress.XtraBars.ItemClickEventHandler(this.btnInvoices_Click);

            this.btnReports.Caption = "التقارير";
            this.btnReports.Id = 4;
            this.btnReports.Name = "btnReports";
            this.btnReports.ItemClick += new DevExpress.XtraBars.ItemClickEventHandler(this.btnReports_Click);

            this.btnUsers.Caption = "المستخدمين";
            this.btnUsers.Id = 5;
            this.btnUsers.Name = "btnUsers";
            this.btnUsers.ItemClick += new DevExpress.XtraBars.ItemClickEventHandler(this.btnUsers_Click);

            this.btnSettings.Caption = "الإعدادات";
            this.btnSettings.Id = 6;
            this.btnSettings.Name = "btnSettings";
            this.btnSettings.ItemClick += new DevExpress.XtraBars.ItemClickEventHandler(this.btnSettings_Click);

            this.btnLogout.Caption = "تسجيل الخروج";
            this.btnLogout.Id = 7;
            this.btnLogout.Name = "btnLogout";
            this.btnLogout.ItemClick += new DevExpress.XtraBars.ItemClickEventHandler(this.btnLogout_Click);

            this.btnRefresh.Caption = "تحديث";
            this.btnRefresh.Id = 8;
            this.btnRefresh.Name = "btnRefresh";
            this.btnRefresh.ItemClick += new DevExpress.XtraBars.ItemClickEventHandler(this.btnRefresh_Click);

            // Ribbon page and groups
            ribbonPage1.Groups.AddRange(new DevExpress.XtraBars.Ribbon.RibbonPageGroup[] {
                ribbonPageGroup1, ribbonPageGroup2, ribbonPageGroup3});
            ribbonPage1.Name = "ribbonPage1";
            ribbonPage1.Text = "نظام إدارة المبيعات";

            ribbonPageGroup1.ItemLinks.Add(this.btnCustomers);
            ribbonPageGroup1.ItemLinks.Add(this.btnProducts);
            ribbonPageGroup1.ItemLinks.Add(this.btnInvoices);
            ribbonPageGroup1.Name = "ribbonPageGroup1";
            ribbonPageGroup1.Text = "البيانات الأساسية";

            ribbonPageGroup2.ItemLinks.Add(this.btnReports);
            ribbonPageGroup2.ItemLinks.Add(this.btnUsers);
            ribbonPageGroup2.ItemLinks.Add(this.btnSettings);
            ribbonPageGroup2.Name = "ribbonPageGroup2";
            ribbonPageGroup2.Text = "الإدارة";

            ribbonPageGroup3.ItemLinks.Add(this.btnRefresh);
            ribbonPageGroup3.ItemLinks.Add(this.btnLogout);
            ribbonPageGroup3.Name = "ribbonPageGroup3";
            ribbonPageGroup3.Text = "النظام";

            // panelControl1
            this.panelControl1.Controls.Add(this.chartControl1);
            this.panelControl1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.panelControl1.Location = new System.Drawing.Point(0, 158);
            this.panelControl1.Name = "panelControl1";
            this.panelControl1.Size = new System.Drawing.Size(900, 402);
            this.panelControl1.TabIndex = 1;

            // panelRecentActivities
            this.panelRecentActivities.Controls.Add(this.gridRecentInvoices);
            this.panelRecentActivities.Controls.Add(this.lblRecentActivitiesTitle);
            this.panelRecentActivities.Dock = System.Windows.Forms.DockStyle.Right;
            this.panelRecentActivities.Location = new System.Drawing.Point(900, 158);
            this.panelRecentActivities.Name = "panelRecentActivities";
            this.panelRecentActivities.Size = new System.Drawing.Size(300, 402);
            this.panelRecentActivities.TabIndex = 3;

            // lblRecentActivitiesTitle
            this.lblRecentActivitiesTitle.Appearance.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Bold);
            this.lblRecentActivitiesTitle.Appearance.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.lblRecentActivitiesTitle.Location = new System.Drawing.Point(10, 10);
            this.lblRecentActivitiesTitle.Name = "lblRecentActivitiesTitle";
            this.lblRecentActivitiesTitle.Size = new System.Drawing.Size(100, 21);
            this.lblRecentActivitiesTitle.TabIndex = 0;
            this.lblRecentActivitiesTitle.Text = "الأنشطة الحديثة";

            // gridRecentInvoices
            this.gridRecentInvoices.Dock = System.Windows.Forms.DockStyle.Fill;
            this.gridRecentInvoices.Location = new System.Drawing.Point(2, 40);
            this.gridRecentInvoices.Name = "gridRecentInvoices";
            this.gridRecentInvoices.Size = new System.Drawing.Size(296, 360);
            this.gridRecentInvoices.TabIndex = 1;
            this.gridRecentInvoices.RightToLeft = System.Windows.Forms.RightToLeft.Yes;

            // chartControl1
            this.chartControl1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.chartControl1.Location = new System.Drawing.Point(2, 2);
            this.chartControl1.Name = "chartControl1";
            this.chartControl1.SeriesSerializable = new DevExpress.XtraCharts.Series[0];
            this.chartControl1.Size = new System.Drawing.Size(1196, 398);
            this.chartControl1.TabIndex = 0;

            // tileControl1
            this.tileControl1.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.tileControl1.Location = new System.Drawing.Point(0, 560);
            this.tileControl1.Name = "tileControl1";
            this.tileControl1.Size = new System.Drawing.Size(1200, 100);
            this.tileControl1.TabIndex = 2;
            this.tileControl1.RightToLeft = System.Windows.Forms.RightToLeft.Yes;

            // panelControl2 (hidden, keeping labels for compatibility)
            this.panelControl2.Controls.Add(this.lblTotalInvoices);
            this.panelControl2.Controls.Add(this.lblTotalProducts);
            this.panelControl2.Controls.Add(this.lblTotalCustomers);
            this.panelControl2.Controls.Add(this.lblTotalSales);
            this.panelControl2.Visible = false;
            this.panelControl2.Location = new System.Drawing.Point(0, 660);
            this.panelControl2.Name = "panelControl2";
            this.panelControl2.Size = new System.Drawing.Size(1200, 100);
            this.panelControl2.TabIndex = 4;

            // Labels
            this.lblTotalSales.Appearance.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Bold);
            this.lblTotalSales.Appearance.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.lblTotalSales.Location = new System.Drawing.Point(50, 20);
            this.lblTotalSales.Name = "lblTotalSales";
            this.lblTotalSales.Size = new System.Drawing.Size(150, 21);
            this.lblTotalSales.TabIndex = 0;
            this.lblTotalSales.Text = "إجمالي المبيعات: 0 ر.س";

            this.lblTotalCustomers.Appearance.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Bold);
            this.lblTotalCustomers.Appearance.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.lblTotalCustomers.Location = new System.Drawing.Point(350, 20);
            this.lblTotalCustomers.Name = "lblTotalCustomers";
            this.lblTotalCustomers.Size = new System.Drawing.Size(90, 21);
            this.lblTotalCustomers.TabIndex = 1;
            this.lblTotalCustomers.Text = "عدد العملاء: 0";

            this.lblTotalProducts.Appearance.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Bold);
            this.lblTotalProducts.Appearance.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.lblTotalProducts.Location = new System.Drawing.Point(650, 20);
            this.lblTotalProducts.Name = "lblTotalProducts";
            this.lblTotalProducts.Size = new System.Drawing.Size(100, 21);
            this.lblTotalProducts.TabIndex = 2;
            this.lblTotalProducts.Text = "عدد المنتجات: 0";

            this.lblTotalInvoices.Appearance.Font = new System.Drawing.Font("Segoe UI", 12F, System.Drawing.FontStyle.Bold);
            this.lblTotalInvoices.Appearance.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.lblTotalInvoices.Location = new System.Drawing.Point(950, 20);
            this.lblTotalInvoices.Name = "lblTotalInvoices";
            this.lblTotalInvoices.Size = new System.Drawing.Size(95, 21);
            this.lblTotalInvoices.TabIndex = 3;
            this.lblTotalInvoices.Text = "عدد الفواتير: 0";

            // MainDashboardForm
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1200, 660);
            this.Controls.Add(this.panelControl1);
            this.Controls.Add(this.panelRecentActivities);
            this.Controls.Add(this.tileControl1);
            this.Controls.Add(this.panelControl2);
            this.Controls.Add(this.ribbonControl1);
            this.IconOptions.ShowIcon = false;
            this.Name = "MainDashboardForm";
            this.Ribbon = this.ribbonControl1;
            this.RightToLeft = System.Windows.Forms.RightToLeft.Yes;
            this.RightToLeftLayout = true;
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "نظام إدارة المبيعات";
            this.WindowState = System.Windows.Forms.FormWindowState.Maximized;
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.MainDashboardForm_FormClosing);

            ((System.ComponentModel.ISupportInitialize)(this.ribbonControl1)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelControl1)).EndInit();
            this.panelControl1.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.chartControl1)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelControl2)).EndInit();
            this.panelControl2.ResumeLayout(false);
            this.panelControl2.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();
        }

        private void SetupForm()
        {
            // إعداد النموذج للغة العربية - Setup form for Arabic
            this.Text = $"نظام إدارة المبيعات - مرحباً {_currentUser?.FullName}";
            this.RightToLeft = RightToLeft.Yes;
            this.RightToLeftLayout = true;
            this.WindowState = FormWindowState.Maximized;
            this.StartPosition = FormStartPosition.CenterScreen;

            // Modern UI styling
            this.LookAndFeel.UseDefaultLookAndFeel = false;
            this.LookAndFeel.SkinName = "WXI";
            this.Appearance.BackColor = Color.FromArgb(248, 249, 250);

            // Set Arabic font with better readability
            this.Font = new Font("Segoe UI", 9.5F, FontStyle.Regular);
        }

        private void SetupButtons()
        {
            // إعداد الأزرار حسب صلاحيات المستخدم - Setup buttons based on user permissions
            if (_currentUser?.Role == null) return;

            // إعداد الأزرار حسب الصلاحيات - Setup buttons based on permissions
            btnCustomers.Enabled = _currentUser.Role.CanManageCustomers;
            btnProducts.Enabled = _currentUser.Role.CanManageProducts;
            btnInvoices.Enabled = _currentUser.Role.CanCreateInvoices;
            btnReports.Enabled = _currentUser.Role.CanViewReports;
            btnUsers.Enabled = _currentUser.Role.CanManageUsers;
            btnSettings.Enabled = _currentUser.Role.CanManageSettings;
        }

        private void SetupTiles()
        {
            // إعداد البلاطات - Setup tiles
            tileControl1.Groups.Clear();

            var group = new DevExpress.XtraEditors.TileGroup();
            group.Text = "الإحصائيات الرئيسية";

            // بلاطة إجمالي المبيعات - Total Sales Tile
            var tileSales = new DevExpress.XtraEditors.TileItem();
            tileSales.Text = "إجمالي المبيعات";
            tileSales.ContentAnimation = DevExpress.XtraEditors.TileItemContentAnimationType.Fade;
            tileSales.BackgroundImageScaleMode = DevExpress.XtraEditors.TileItemImageScaleMode.ZoomInside;
            tileSales.ItemSize = DevExpress.XtraEditors.TileItemSize.Medium;
            tileSales.Tag = "sales";

            // بلاطة عدد العملاء - Customers Count Tile
            var tileCustomers = new DevExpress.XtraEditors.TileItem();
            tileCustomers.Text = "عدد العملاء";
            tileCustomers.ContentAnimation = DevExpress.XtraEditors.TileItemContentAnimationType.Fade;
            tileCustomers.ItemSize = DevExpress.XtraEditors.TileItemSize.Medium;
            tileCustomers.Tag = "customers";

            // بلاطة عدد المنتجات - Products Count Tile
            var tileProducts = new DevExpress.XtraEditors.TileItem();
            tileProducts.Text = "عدد المنتجات";
            tileProducts.ContentAnimation = DevExpress.XtraEditors.TileItemContentAnimationType.Fade;
            tileProducts.ItemSize = DevExpress.XtraEditors.TileItemSize.Medium;
            tileProducts.Tag = "products";

            // بلاطة عدد الفواتير - Invoices Count Tile
            var tileInvoices = new DevExpress.XtraEditors.TileItem();
            tileInvoices.Text = "عدد الفواتير";
            tileInvoices.ContentAnimation = DevExpress.XtraEditors.TileItemContentAnimationType.Fade;
            tileInvoices.ItemSize = DevExpress.XtraEditors.TileItemSize.Medium;
            tileInvoices.Tag = "invoices";

            // إضافة البلاطات إلى المجموعة - Add tiles to group
            group.Items.Add(tileSales);
            group.Items.Add(tileCustomers);
            group.Items.Add(tileProducts);
            group.Items.Add(tileInvoices);

            tileControl1.Groups.Add(group);
        }

        private async Task LoadDashboardDataAsync()
        {
            try
            {
                // تحميل بيانات لوحة التحكم - Load dashboard data
                await LoadSummaryDataAsync();
                await LoadRecentActivitiesAsync();
                await LoadChartDataAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"خطأ في تحميل بيانات لوحة التحكم:\n{ex.Message}",
                    "خطأ", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private async Task LoadSummaryDataAsync()
        {
            try
            {
                // تحميل الإحصائيات من قاعدة البيانات - Load statistics from database
                var summary = await _dashboardService.GetDashboardSummaryAsync();

                // تحديث التسميات - Update labels
                lblTotalSales.Text = $"إجمالي المبيعات: {summary.TotalSales:N2} ر.س";
                lblTotalCustomers.Text = $"عدد العملاء: {summary.TotalCustomers:N0}";
                lblTotalProducts.Text = $"عدد المنتجات: {summary.TotalProducts:N0}";
                lblTotalInvoices.Text = $"عدد الفواتير: {summary.TotalInvoices:N0}";

                // تحديث البلاطات - Update tiles
                UpdateTiles(summary);
            }
            catch (Exception ex)
            {
                // في حالة الخطأ، عرض القيم الافتراضية - In case of error, show default values
                var defaultSummary = new DashboardSummary
                {
                    TotalSales = 0,
                    TotalCustomers = 0,
                    TotalProducts = 0,
                    TotalInvoices = 0
                };

                lblTotalSales.Text = "إجمالي المبيعات: 0 ر.س";
                lblTotalCustomers.Text = "عدد العملاء: 0";
                lblTotalProducts.Text = "عدد المنتجات: 0";
                lblTotalInvoices.Text = "عدد الفواتير: 0";

                // تحديث البلاطات بالقيم الافتراضية - Update tiles with default values
                UpdateTiles(defaultSummary);

                // تسجيل الخطأ - Log error
                System.Diagnostics.Debug.WriteLine($"Error loading summary data: {ex.Message}");
            }
        }

        private void UpdateTiles(DashboardSummary summary)
        {
            // تحديث البلاطات بطريقة مبسطة - Update tiles in a simplified way
            foreach (DevExpress.XtraEditors.TileGroup group in tileControl1.Groups)
            {
                foreach (DevExpress.XtraEditors.TileItem tile in group.Items)
                {
                    if (tile.Tag != null)
                    {
                        switch (tile.Tag.ToString())
                        {
                            case "sales":
                                tile.Text = $"إجمالي المبيعات\n{summary.TotalSales:N2} ر.س";
                                tile.AppearanceItem.Normal.Font = new Font("Segoe UI", 10, FontStyle.Bold);
                                tile.AppearanceItem.Normal.ForeColor = Color.White;
                                tile.AppearanceItem.Normal.BackColor = Color.FromArgb(0, 122, 204);
                                break;
                            case "customers":
                                tile.Text = $"عدد العملاء\n{summary.TotalCustomers:N0}";
                                tile.AppearanceItem.Normal.Font = new Font("Segoe UI", 10, FontStyle.Bold);
                                tile.AppearanceItem.Normal.ForeColor = Color.White;
                                tile.AppearanceItem.Normal.BackColor = Color.FromArgb(40, 167, 69);
                                break;
                            case "products":
                                tile.Text = $"عدد المنتجات\n{summary.TotalProducts:N0}";
                                tile.AppearanceItem.Normal.Font = new Font("Segoe UI", 10, FontStyle.Bold);
                                tile.AppearanceItem.Normal.ForeColor = Color.White;
                                tile.AppearanceItem.Normal.BackColor = Color.FromArgb(255, 193, 7);
                                break;
                            case "invoices":
                                tile.Text = $"عدد الفواتير\n{summary.TotalInvoices:N0}";
                                tile.AppearanceItem.Normal.Font = new Font("Segoe UI", 10, FontStyle.Bold);
                                tile.AppearanceItem.Normal.ForeColor = Color.White;
                                tile.AppearanceItem.Normal.BackColor = Color.FromArgb(220, 53, 69);
                                break;
                        }
                    }
                }
            }
        }

        private async Task LoadRecentActivitiesAsync()
        {
            try
            {
                // تحميل الأنشطة الحديثة - Load recent activities
                var recentInvoices = await _dashboardService.GetRecentInvoicesAsync(10);

                // إعداد بيانات الشبكة - Setup grid data
                var invoiceData = recentInvoices.Select(i => new
                {
                    InvoiceNumber = i.InvoiceNumber,
                    CustomerName = i.Customer?.CustomerName ?? "غير محدد",
                    TotalAmount = i.TotalAmount,
                    InvoiceDate = i.InvoiceDate.ToString("dd/MM/yyyy"),
                    Status = GetInvoiceStatusText(i.Status)
                }).ToList();

                gridRecentInvoices.DataSource = invoiceData;

                // إعداد عرض الشبكة - Setup grid view
                var gridView = gridRecentInvoices.MainView as DevExpress.XtraGrid.Views.Grid.GridView;
                if (gridView != null)
                {
                    gridView.Columns["InvoiceNumber"].Caption = "رقم الفاتورة";
                    gridView.Columns["CustomerName"].Caption = "العميل";
                    gridView.Columns["TotalAmount"].Caption = "المبلغ";
                    gridView.Columns["InvoiceDate"].Caption = "التاريخ";
                    gridView.Columns["Status"].Caption = "الحالة";

                    // تنسيق العملة - Format currency
                    gridView.Columns["TotalAmount"].DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
                    gridView.Columns["TotalAmount"].DisplayFormat.FormatString = "N2";

                    gridView.BestFitColumns();
                    gridView.OptionsView.ShowGroupPanel = false;
                    gridView.OptionsView.ColumnAutoWidth = true;
                }
            }
            catch (Exception ex)
            {
                // تسجيل الخطأ - Log error
                System.Diagnostics.Debug.WriteLine($"Error loading recent activities: {ex.Message}");
            }
        }

        private static string GetInvoiceStatusText(InvoiceStatus status)
        {
            return status switch
            {
                InvoiceStatus.Draft => "مسودة",
                InvoiceStatus.Pending => "معلقة",
                InvoiceStatus.Paid => "مدفوعة",
                InvoiceStatus.Cancelled => "ملغية",
                InvoiceStatus.Overdue => "متأخرة",
                _ => "غير محدد"
            };
        }

        private async Task LoadChartDataAsync()
        {
            try
            {
                // تحميل بيانات المخططات - Load chart data
                await LoadSalesChartAsync();
                await LoadTopProductsChartAsync();
            }
            catch (Exception ex)
            {
                // تسجيل الخطأ - Log error
                System.Diagnostics.Debug.WriteLine($"Error loading chart data: {ex.Message}");
            }
        }

        private async Task LoadSalesChartAsync()
        {
            try
            {
                // تحميل بيانات المبيعات الشهرية - Load monthly sales data
                var salesData = await _dashboardService.GetMonthlySalesDataAsync(12);

                // إعداد المخطط - Setup chart
                chartControl1.Series.Clear();

                // إنشاء سلسلة بيانات - Create data series
                var series = new DevExpress.XtraCharts.Series("المبيعات الشهرية", DevExpress.XtraCharts.ViewType.Line);

                // إضافة البيانات - Add data points
                foreach (var data in salesData)
                {
                    series.Points.Add(new DevExpress.XtraCharts.SeriesPoint(data.Month, data.Sales));
                }

                chartControl1.Series.Add(series);

                // إعداد المحاور - Setup axes
                var diagram = (DevExpress.XtraCharts.XYDiagram)chartControl1.Diagram;
                diagram.AxisX.Title.Text = "الشهر";
                diagram.AxisY.Title.Text = "المبيعات (ر.س)";
                diagram.AxisX.Title.Visibility = DevExpress.Utils.DefaultBoolean.True;
                diagram.AxisY.Title.Visibility = DevExpress.Utils.DefaultBoolean.True;

                // إعداد العنوان - Setup title
                chartControl1.Titles.Clear();
                var title = new DevExpress.XtraCharts.ChartTitle();
                title.Text = "مخطط المبيعات الشهرية";
                title.Font = new Font("Segoe UI", 14, FontStyle.Bold);
                chartControl1.Titles.Add(title);
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error loading sales chart: {ex.Message}");
            }
        }

        private async Task LoadTopProductsChartAsync()
        {
            try
            {
                // تحميل بيانات أفضل المنتجات - Load top products data
                var topProducts = await _dashboardService.GetTopProductsDataAsync(10);

                // يمكن إضافة مخطط ثانوي أو تبديل المخطط الحالي
                // Can add secondary chart or switch current chart

                // للآن، سنعرض فقط مخطط المبيعات الشهرية
                // For now, we'll only show monthly sales chart
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Error loading top products chart: {ex.Message}");
            }
        }

        // معالجات الأحداث - Event Handlers
        private void btnCustomers_Click(object sender, DevExpress.XtraBars.ItemClickEventArgs e)
        {
            try
            {
                // فتح نموذج إدارة العملاء - Open customers management form
                var customersForm = new CustomersForm();
                customersForm.ShowDialog();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في فتح نموذج العملاء:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                System.Diagnostics.Debug.WriteLine($"خطأ في فتح نموذج العملاء: {ex}");
            }
        }

        private void btnProducts_Click(object sender, DevExpress.XtraBars.ItemClickEventArgs e)
        {
            try
            {
                // فتح نموذج إدارة المنتجات - Open products management form
                var productsForm = new ProductsForm();
                productsForm.ShowDialog();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في فتح نموذج المنتجات:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                System.Diagnostics.Debug.WriteLine($"خطأ في فتح نموذج المنتجات: {ex}");
            }
        }

        private void btnInvoices_Click(object sender, DevExpress.XtraBars.ItemClickEventArgs e)
        {
            try
            {
                // فتح نموذج إدارة الفواتير - Open invoices management form
                var invoicesForm = new InvoicesForm();
                invoicesForm.ShowDialog();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في فتح نموذج الفواتير:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                System.Diagnostics.Debug.WriteLine($"خطأ في فتح نموذج الفواتير: {ex}");
            }
        }

        private void btnReports_Click(object sender, DevExpress.XtraBars.ItemClickEventArgs e)
        {
            try
            {
                // فتح نموذج التقارير - Open reports form
                var reportsForm = new ReportsForm();
                reportsForm.ShowDialog();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في فتح نموذج التقارير:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                System.Diagnostics.Debug.WriteLine($"خطأ في فتح نموذج التقارير: {ex}");
            }
        }

        private void btnUsers_Click(object sender, DevExpress.XtraBars.ItemClickEventArgs e)
        {
            try
            {
                // فتح نموذج إدارة المستخدمين - Open users management form
                var usersForm = new UsersForm();
                usersForm.ShowDialog();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في فتح نموذج المستخدمين:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                System.Diagnostics.Debug.WriteLine($"خطأ في فتح نموذج المستخدمين: {ex}");
            }
        }

        private void btnSettings_Click(object sender, DevExpress.XtraBars.ItemClickEventArgs e)
        {
            try
            {
                // فتح نموذج الإعدادات - Open settings form
                var settingsForm = new SettingsForm();
                settingsForm.ShowDialog();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في فتح نموذج الإعدادات:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
                System.Diagnostics.Debug.WriteLine($"خطأ في فتح نموذج الإعدادات: {ex}");
            }
        }

        private void MainDashboardForm_FormClosing(object? sender, FormClosingEventArgs e)
        {
            var result = MessageBox.Show("هل تريد إغلاق النظام؟", "تأكيد الإغلاق",
                MessageBoxButtons.YesNo, MessageBoxIcon.Question);

            if (result != DialogResult.Yes)
            {
                e.Cancel = true;
            }
        }

        private void btnLogout_Click(object sender, DevExpress.XtraBars.ItemClickEventArgs e)
        {
            var result = MessageBox.Show("هل تريد تسجيل الخروج؟", "تأكيد تسجيل الخروج",
                MessageBoxButtons.YesNo, MessageBoxIcon.Question);

            if (result == DialogResult.Yes)
            {
                this.DialogResult = DialogResult.OK;
                this.Close();
            }
        }

        private async void btnRefresh_Click(object sender, DevExpress.XtraBars.ItemClickEventArgs e)
        {
            await LoadingHelper.ExecuteWithLoadingAsync(async () =>
            {
                await LoadDashboardDataAsync();
                LoadingHelper.ShowSuccess("تم تحديث بيانات لوحة التحكم بنجاح", "تحديث");
            }, "جاري تحديث بيانات لوحة التحكم...", this);
        }
    }


}
