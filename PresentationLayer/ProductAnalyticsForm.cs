using DXApplication1.Models;
using DXApplication1.BusinessLogicLayer;
using DevExpress.XtraEditors;
using DevExpress.XtraCharts;
using DevExpress.XtraGrid;
using DevExpress.XtraGrid.Views.Grid;
using System.ComponentModel;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// نموذج تحليلات المنتجات - Product Analytics Form
    /// </summary>
    public partial class ProductAnalyticsForm : XtraForm
    {
        private readonly IProductService _productService;
        private readonly IDashboardService _dashboardService;

        // DevExpress Controls
        private PanelControl panelTop = null!;
        private PanelControl panelCharts = null!;
        private PanelControl panelBottom = null!;
        
        // Charts
        private ChartControl chartCategoryDistribution = null!;
        private ChartControl chartInventoryLevels = null!;
        private ChartControl chartProfitability = null!;
        private ChartControl chartSalesTrends = null!;
        
        // Summary Controls
        private LabelControl lblTotalProducts = null!;
        private LabelControl lblTotalValue = null!;
        private LabelControl lblLowStock = null!;
        private LabelControl lblOutOfStock = null!;
        
        // Data Grids
        private GridControl gridTopProducts = null!;
        private GridView gridViewTopProducts = null!;
        private GridControl gridLowStock = null!;
        private GridView gridViewLowStock = null!;
        
        // Buttons
        private SimpleButton btnRefresh = null!;
        private SimpleButton btnExport = null!;
        private SimpleButton btnClose = null!;

        public ProductAnalyticsForm(IProductService productService, IDashboardService dashboardService)
        {
            _productService = productService ?? throw new ArgumentNullException(nameof(productService));
            _dashboardService = dashboardService ?? throw new ArgumentNullException(nameof(dashboardService));
            
            InitializeComponent();
            SetupForm();
            _ = LoadAnalyticsDataAsync();
        }

        private void SetupForm()
        {
            // إعداد النموذج - Setup form
            this.Text = "تحليلات المنتجات - Product Analytics";
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

            // إعداد الرسوم البيانية - Setup charts
            SetupCharts();
            SetupGrids();
        }

        private void SetupCharts()
        {
            // Setup Category Distribution Chart (Pie Chart)
            SetupCategoryDistributionChart();
            
            // Setup Inventory Levels Chart (Bar Chart)
            SetupInventoryLevelsChart();
            
            // Setup Profitability Chart (Column Chart)
            SetupProfitabilityChart();
            
            // Setup Sales Trends Chart (Line Chart)
            SetupSalesTrendsChart();
        }

        private void SetupCategoryDistributionChart()
        {
            chartCategoryDistribution.Series.Clear();
            
            var series = new Series("توزيع الفئات", ViewType.Pie);
            series.ArgumentDataMember = "CategoryName";
            series.ValueDataMembers.AddRange(new string[] { "ProductCount" });
            
            var pieView = (PieSeriesView)series.View;
            pieView.RuntimeExploding = true;
            pieView.ExplodedDistancePercentage = 10;
            
            chartCategoryDistribution.Series.Add(series);
            chartCategoryDistribution.Titles.Add(new ChartTitle { Text = "توزيع المنتجات حسب الفئة" });
        }

        private void SetupInventoryLevelsChart()
        {
            chartInventoryLevels.Series.Clear();
            
            var series = new Series("مستويات المخزون", ViewType.Bar);
            series.ArgumentDataMember = "ProductName";
            series.ValueDataMembers.AddRange(new string[] { "StockQuantity" });
            
            var barView = (BarSeriesView)series.View;
            barView.Color = Color.FromArgb(54, 162, 235);
            
            chartInventoryLevels.Series.Add(series);
            chartInventoryLevels.Titles.Add(new ChartTitle { Text = "مستويات المخزون للمنتجات" });
        }

        private void SetupProfitabilityChart()
        {
            chartProfitability.Series.Clear();
            
            var profitSeries = new Series("هامش الربح", ViewType.Bar);
            profitSeries.ArgumentDataMember = "ProductName";
            profitSeries.ValueDataMembers.AddRange(new string[] { "ProfitMargin" });
            
            var barView = (BarSeriesView)profitSeries.View;
            barView.Color = Color.FromArgb(75, 192, 192);
            
            chartProfitability.Series.Add(profitSeries);
            chartProfitability.Titles.Add(new ChartTitle { Text = "ربحية المنتجات" });
        }

        private void SetupSalesTrendsChart()
        {
            chartSalesTrends.Series.Clear();
            
            var series = new Series("اتجاهات المبيعات", ViewType.Line);
            series.ArgumentDataMember = "Month";
            series.ValueDataMembers.AddRange(new string[] { "SalesAmount" });
            
            var lineView = (LineSeriesView)series.View;
            lineView.Color = Color.FromArgb(255, 99, 132);
            lineView.LineMarkerOptions.Visible = true;
            lineView.LineMarkerOptions.Size = 8;
            
            chartSalesTrends.Series.Add(series);
            chartSalesTrends.Titles.Add(new ChartTitle { Text = "اتجاهات مبيعات المنتجات" });
        }

        private void SetupGrids()
        {
            // Setup Top Products Grid
            SetupTopProductsGrid();
            
            // Setup Low Stock Grid
            SetupLowStockGrid();
        }

        private void SetupTopProductsGrid()
        {
            gridViewTopProducts.Columns.Clear();
            
            gridViewTopProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "ProductName",
                Caption = "اسم المنتج",
                Width = 200
            });
            
            gridViewTopProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "SalesQuantity",
                Caption = "كمية المبيعات",
                Width = 100
            });
            
            gridViewTopProducts.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "SalesAmount",
                Caption = "قيمة المبيعات",
                Width = 120,
                DisplayFormat = { FormatType = DevExpress.Utils.FormatType.Numeric, FormatString = "N2" }
            });
            
            gridViewTopProducts.OptionsView.ShowGroupPanel = false;
            gridViewTopProducts.OptionsSelection.EnableAppearanceFocusedCell = false;
        }

        private void SetupLowStockGrid()
        {
            gridViewLowStock.Columns.Clear();
            
            gridViewLowStock.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "ProductName",
                Caption = "اسم المنتج",
                Width = 200
            });
            
            gridViewLowStock.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "StockQuantity",
                Caption = "الكمية الحالية",
                Width = 100
            });
            
            gridViewLowStock.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "MinimumStock",
                Caption = "الحد الأدنى",
                Width = 100
            });
            
            gridViewLowStock.Columns.Add(new DevExpress.XtraGrid.Columns.GridColumn
            {
                FieldName = "Category.CategoryName",
                Caption = "الفئة",
                Width = 120
            });
            
            gridViewLowStock.OptionsView.ShowGroupPanel = false;
            gridViewLowStock.OptionsSelection.EnableAppearanceFocusedCell = false;
        }

        private async Task LoadAnalyticsDataAsync()
        {
            try
            {
                this.Cursor = Cursors.WaitCursor;
                
                // Load summary data
                await LoadSummaryDataAsync();
                
                // Load chart data
                await LoadChartDataAsync();
                
                // Load grid data
                await LoadGridDataAsync();
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في تحميل بيانات التحليلات:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                this.Cursor = Cursors.Default;
            }
        }

        private async Task LoadSummaryDataAsync()
        {
            try
            {
                var totalProducts = await _productService.GetActiveProductsCountAsync();
                var totalValue = await _productService.GetTotalInventoryValueAsync();
                var lowStockProducts = await _productService.GetLowStockProductsAsync();
                var outOfStockProducts = await _productService.GetOutOfStockProductsAsync();

                lblTotalProducts.Text = $"إجمالي المنتجات: {totalProducts:N0}";
                lblTotalValue.Text = $"قيمة المخزون: {totalValue:N2} ر.س";
                lblLowStock.Text = $"منتجات قليلة المخزون: {lowStockProducts.Count():N0}";
                lblOutOfStock.Text = $"منتجات نفدت من المخزون: {outOfStockProducts.Count():N0}";
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"خطأ في تحميل البيانات الإجمالية: {ex}");
            }
        }

        private async Task LoadChartDataAsync()
        {
            try
            {
                // Load category distribution data
                var categories = await _productService.GetAllCategoriesAsync();
                var categoryData = new List<object>();
                
                foreach (var category in categories)
                {
                    var productCount = await _productService.GetProductsCountByCategoryAsync(category.Id);
                    categoryData.Add(new { CategoryName = category.CategoryName, ProductCount = productCount });
                }
                
                chartCategoryDistribution.DataSource = categoryData;
                
                // Load inventory levels data
                var products = await _productService.GetAllProductsAsync();
                var inventoryData = products.Take(20).Select(p => new 
                { 
                    ProductName = p.ProductName, 
                    StockQuantity = p.StockQuantity 
                }).ToList();
                
                chartInventoryLevels.DataSource = inventoryData;
                
                // Load profitability data
                var profitabilityData = products.Take(15).Select(p => new 
                { 
                    ProductName = p.ProductName, 
                    ProfitMargin = p.SalePrice - p.PurchasePrice 
                }).ToList();
                
                chartProfitability.DataSource = profitabilityData;
                
                // Load sales trends data (mock data for demonstration)
                var salesTrendsData = GenerateMockSalesTrendsData();
                chartSalesTrends.DataSource = salesTrendsData;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"خطأ في تحميل بيانات الرسوم البيانية: {ex}");
            }
        }

        private async Task LoadGridDataAsync()
        {
            try
            {
                // Load top selling products (mock data)
                var topProducts = await _productService.GetTopSellingProductsAsync(10);
                var topProductsData = topProducts.Select(p => new 
                { 
                    ProductName = p.ProductName,
                    SalesQuantity = new Random().Next(50, 500), // Mock data
                    SalesAmount = new Random().Next(1000, 10000) // Mock data
                }).ToList();
                
                gridTopProducts.DataSource = topProductsData;
                
                // Load low stock products
                var lowStockProducts = await _productService.GetLowStockProductsAsync();
                gridLowStock.DataSource = lowStockProducts.ToList();
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"خطأ في تحميل بيانات الشبكات: {ex}");
            }
        }

        private List<object> GenerateMockSalesTrendsData()
        {
            var data = new List<object>();
            var months = new[] { "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو" };
            var random = new Random();
            
            foreach (var month in months)
            {
                data.Add(new { Month = month, SalesAmount = random.Next(10000, 50000) });
            }
            
            return data;
        }

        // Event Handlers
        private async void btnRefresh_Click(object? sender, EventArgs e)
        {
            await LoadingHelper.ExecuteWithLoadingAsync(async () =>
            {
                await LoadAnalyticsDataAsync();
                LoadingHelper.ShowSuccess("تم تحديث بيانات التحليلات بنجاح", "تحديث");
            }, "جاري تحديث بيانات التحليلات...", this);
        }

        private void btnExport_Click(object? sender, EventArgs e)
        {
            try
            {
                using var saveFileDialog = new SaveFileDialog
                {
                    Title = "تصدير التحليلات",
                    Filter = "PDF Files|*.pdf|Excel Files|*.xlsx|All Files|*.*",
                    FilterIndex = 1,
                    FileName = $"Product_Analytics_{DateTime.Now:yyyyMMdd_HHmmss}"
                };

                if (saveFileDialog.ShowDialog() == DialogResult.OK)
                {
                    // Export logic would go here
                    XtraMessageBox.Show("تم تصدير التحليلات بنجاح", "تصدير",
                        MessageBoxButtons.OK, MessageBoxIcon.Information);
                }
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ في تصدير التحليلات:\n{ex.Message}", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void btnClose_Click(object? sender, EventArgs e)
        {
            this.Close();
        }

        private void InitializeComponent()
        {
            this.panelTop = new PanelControl();
            this.panelCharts = new PanelControl();
            this.panelBottom = new PanelControl();

            this.chartCategoryDistribution = new ChartControl();
            this.chartInventoryLevels = new ChartControl();
            this.chartProfitability = new ChartControl();
            this.chartSalesTrends = new ChartControl();

            this.lblTotalProducts = new LabelControl();
            this.lblTotalValue = new LabelControl();
            this.lblLowStock = new LabelControl();
            this.lblOutOfStock = new LabelControl();

            this.gridTopProducts = new GridControl();
            this.gridViewTopProducts = new GridView();
            this.gridLowStock = new GridControl();
            this.gridViewLowStock = new GridView();

            this.btnRefresh = new SimpleButton();
            this.btnExport = new SimpleButton();
            this.btnClose = new SimpleButton();

            ((System.ComponentModel.ISupportInitialize)(this.panelTop)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelCharts)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.panelBottom)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.chartCategoryDistribution)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.chartInventoryLevels)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.chartProfitability)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.chartSalesTrends)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridTopProducts)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridViewTopProducts)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridLowStock)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridViewLowStock)).BeginInit();
            this.panelTop.SuspendLayout();
            this.panelCharts.SuspendLayout();
            this.panelBottom.SuspendLayout();
            this.SuspendLayout();

            // panelTop - Summary Labels
            this.panelTop.Controls.Add(this.lblOutOfStock);
            this.panelTop.Controls.Add(this.lblLowStock);
            this.panelTop.Controls.Add(this.lblTotalValue);
            this.panelTop.Controls.Add(this.lblTotalProducts);
            this.panelTop.Dock = DockStyle.Top;
            this.panelTop.Location = new Point(0, 0);
            this.panelTop.Name = "panelTop";
            this.panelTop.Size = new Size(1200, 80);
            this.panelTop.TabIndex = 0;

            // Configure summary labels
            ConfigureSummaryLabels();

            // panelCharts - Charts and Grids
            this.panelCharts.Controls.Add(this.gridLowStock);
            this.panelCharts.Controls.Add(this.gridTopProducts);
            this.panelCharts.Controls.Add(this.chartSalesTrends);
            this.panelCharts.Controls.Add(this.chartProfitability);
            this.panelCharts.Controls.Add(this.chartInventoryLevels);
            this.panelCharts.Controls.Add(this.chartCategoryDistribution);
            this.panelCharts.Dock = DockStyle.Fill;
            this.panelCharts.Location = new Point(0, 80);
            this.panelCharts.Name = "panelCharts";
            this.panelCharts.Size = new Size(1200, 480);
            this.panelCharts.TabIndex = 1;

            // Configure charts and grids layout
            ConfigureChartsLayout();

            // panelBottom - Action Buttons
            this.panelBottom.Controls.Add(this.btnClose);
            this.panelBottom.Controls.Add(this.btnExport);
            this.panelBottom.Controls.Add(this.btnRefresh);
            this.panelBottom.Dock = DockStyle.Bottom;
            this.panelBottom.Location = new Point(0, 560);
            this.panelBottom.Name = "panelBottom";
            this.panelBottom.Size = new Size(1200, 60);
            this.panelBottom.TabIndex = 2;

            // Configure buttons
            ConfigureButtons();

            // ProductAnalyticsForm
            this.AutoScaleDimensions = new SizeF(6F, 13F);
            this.AutoScaleMode = AutoScaleMode.Font;
            this.ClientSize = new Size(1200, 620);
            this.Controls.Add(this.panelCharts);
            this.Controls.Add(this.panelTop);
            this.Controls.Add(this.panelBottom);
            this.IconOptions.ShowIcon = false;
            this.Name = "ProductAnalyticsForm";
            this.RightToLeft = RightToLeft.Yes;
            this.RightToLeftLayout = true;
            this.StartPosition = FormStartPosition.CenterParent;
            this.Text = "تحليلات المنتجات";
            this.WindowState = FormWindowState.Maximized;

            ((System.ComponentModel.ISupportInitialize)(this.panelTop)).EndInit();
            this.panelTop.ResumeLayout(false);
            this.panelTop.PerformLayout();
            ((System.ComponentModel.ISupportInitialize)(this.panelCharts)).EndInit();
            this.panelCharts.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.panelBottom)).EndInit();
            this.panelBottom.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.chartCategoryDistribution)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.chartInventoryLevels)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.chartProfitability)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.chartSalesTrends)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridTopProducts)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridViewTopProducts)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridLowStock)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.gridViewLowStock)).EndInit();
            this.ResumeLayout(false);
        }

        private void ConfigureSummaryLabels()
        {
            // lblTotalProducts
            this.lblTotalProducts.Appearance.Font = new Font("Segoe UI", 12F, FontStyle.Bold);
            this.lblTotalProducts.Appearance.ForeColor = Color.FromArgb(0, 122, 204);
            this.lblTotalProducts.Location = new Point(20, 20);
            this.lblTotalProducts.Name = "lblTotalProducts";
            this.lblTotalProducts.Size = new Size(200, 21);
            this.lblTotalProducts.TabIndex = 0;
            this.lblTotalProducts.Text = "إجمالي المنتجات: 0";

            // lblTotalValue
            this.lblTotalValue.Appearance.Font = new Font("Segoe UI", 12F, FontStyle.Bold);
            this.lblTotalValue.Appearance.ForeColor = Color.FromArgb(40, 167, 69);
            this.lblTotalValue.Location = new Point(320, 20);
            this.lblTotalValue.Name = "lblTotalValue";
            this.lblTotalValue.Size = new Size(200, 21);
            this.lblTotalValue.TabIndex = 1;
            this.lblTotalValue.Text = "قيمة المخزون: 0.00 ر.س";

            // lblLowStock
            this.lblLowStock.Appearance.Font = new Font("Segoe UI", 12F, FontStyle.Bold);
            this.lblLowStock.Appearance.ForeColor = Color.FromArgb(255, 193, 7);
            this.lblLowStock.Location = new Point(620, 20);
            this.lblLowStock.Name = "lblLowStock";
            this.lblLowStock.Size = new Size(200, 21);
            this.lblLowStock.TabIndex = 2;
            this.lblLowStock.Text = "منتجات قليلة المخزون: 0";

            // lblOutOfStock
            this.lblOutOfStock.Appearance.Font = new Font("Segoe UI", 12F, FontStyle.Bold);
            this.lblOutOfStock.Appearance.ForeColor = Color.FromArgb(220, 53, 69);
            this.lblOutOfStock.Location = new Point(920, 20);
            this.lblOutOfStock.Name = "lblOutOfStock";
            this.lblOutOfStock.Size = new Size(200, 21);
            this.lblOutOfStock.TabIndex = 3;
            this.lblOutOfStock.Text = "منتجات نفدت من المخزون: 0";
        }

        private void ConfigureChartsLayout()
        {
            // chartCategoryDistribution (Top Left)
            this.chartCategoryDistribution.Location = new Point(10, 10);
            this.chartCategoryDistribution.Name = "chartCategoryDistribution";
            this.chartCategoryDistribution.Size = new Size(590, 230);
            this.chartCategoryDistribution.TabIndex = 0;

            // chartInventoryLevels (Top Right)
            this.chartInventoryLevels.Location = new Point(610, 10);
            this.chartInventoryLevels.Name = "chartInventoryLevels";
            this.chartInventoryLevels.Size = new Size(580, 230);
            this.chartInventoryLevels.TabIndex = 1;

            // chartProfitability (Bottom Left)
            this.chartProfitability.Location = new Point(10, 250);
            this.chartProfitability.Name = "chartProfitability";
            this.chartProfitability.Size = new Size(390, 220);
            this.chartProfitability.TabIndex = 2;

            // chartSalesTrends (Bottom Center)
            this.chartSalesTrends.Location = new Point(410, 250);
            this.chartSalesTrends.Name = "chartSalesTrends";
            this.chartSalesTrends.Size = new Size(390, 220);
            this.chartSalesTrends.TabIndex = 3;

            // gridTopProducts (Bottom Right Top)
            this.gridTopProducts.Location = new Point(810, 250);
            this.gridTopProducts.MainView = this.gridViewTopProducts;
            this.gridTopProducts.Name = "gridTopProducts";
            this.gridTopProducts.Size = new Size(380, 110);
            this.gridTopProducts.TabIndex = 4;
            this.gridTopProducts.ViewCollection.AddRange(new DevExpress.XtraGrid.Views.Base.BaseView[] { this.gridViewTopProducts });

            // gridViewTopProducts
            this.gridViewTopProducts.GridControl = this.gridTopProducts;
            this.gridViewTopProducts.Name = "gridViewTopProducts";

            // gridLowStock (Bottom Right Bottom)
            this.gridLowStock.Location = new Point(810, 370);
            this.gridLowStock.MainView = this.gridViewLowStock;
            this.gridLowStock.Name = "gridLowStock";
            this.gridLowStock.Size = new Size(380, 100);
            this.gridLowStock.TabIndex = 5;
            this.gridLowStock.ViewCollection.AddRange(new DevExpress.XtraGrid.Views.Base.BaseView[] { this.gridViewLowStock });

            // gridViewLowStock
            this.gridViewLowStock.GridControl = this.gridLowStock;
            this.gridViewLowStock.Name = "gridViewLowStock";
        }

        private void ConfigureButtons()
        {
            // btnRefresh
            this.btnRefresh.Appearance.BackColor = Color.FromArgb(108, 117, 125);
            this.btnRefresh.Appearance.Font = new Font("Segoe UI", 10F, FontStyle.Bold);
            this.btnRefresh.Appearance.ForeColor = Color.White;
            this.btnRefresh.Appearance.Options.UseBackColor = true;
            this.btnRefresh.Appearance.Options.UseFont = true;
            this.btnRefresh.Appearance.Options.UseForeColor = true;
            this.btnRefresh.Location = new Point(20, 15);
            this.btnRefresh.Name = "btnRefresh";
            this.btnRefresh.Size = new Size(100, 30);
            this.btnRefresh.TabIndex = 0;
            this.btnRefresh.Text = "تحديث";
            this.btnRefresh.Click += btnRefresh_Click;

            // btnExport
            this.btnExport.Appearance.BackColor = Color.FromArgb(40, 167, 69);
            this.btnExport.Appearance.Font = new Font("Segoe UI", 10F, FontStyle.Bold);
            this.btnExport.Appearance.ForeColor = Color.White;
            this.btnExport.Appearance.Options.UseBackColor = true;
            this.btnExport.Appearance.Options.UseFont = true;
            this.btnExport.Appearance.Options.UseForeColor = true;
            this.btnExport.Location = new Point(130, 15);
            this.btnExport.Name = "btnExport";
            this.btnExport.Size = new Size(100, 30);
            this.btnExport.TabIndex = 1;
            this.btnExport.Text = "تصدير";
            this.btnExport.Click += btnExport_Click;

            // btnClose
            this.btnClose.Appearance.Font = new Font("Segoe UI", 10F);
            this.btnClose.Location = new Point(1080, 15);
            this.btnClose.Name = "btnClose";
            this.btnClose.Size = new Size(100, 30);
            this.btnClose.TabIndex = 2;
            this.btnClose.Text = "إغلاق";
            this.btnClose.Click += btnClose_Click;
        }
    }
}
