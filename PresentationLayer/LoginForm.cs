using DXApplication1.Utilities;
using DXApplication1.Models;
using DXApplication1.BusinessLogicLayer;
using DXApplication1.DataAccessLayer;
using System.Drawing;
using System.Windows.Forms;
using DevExpress.XtraEditors;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// نموذج تسجيل الدخول - Login Form
    /// </summary>
    public partial class LoginForm : XtraForm
    {
        private TextEdit txtUsername = null!;
        private TextEdit txtPassword = null!;
        private SimpleButton btnLogin = null!;
        private SimpleButton btnCancel = null!;
        private LabelControl lblTitle = null!;
        private LabelControl lblUsername = null!;
        private LabelControl lblPassword = null!;
        private PictureEdit pictureEdit1 = null!;
        private IUserService _userService = null!;

        public LoginForm()
        {
            InitializeComponent();
            InitializeServices();
            SetupForm();
        }

        private void InitializeServices()
        {
            // إنشاء الخدمات - Initialize services
            var connectionString = System.Configuration.ConfigurationManager.ConnectionStrings["DefaultConnection"]?.ConnectionString
                ?? "Server=.;Database=FirstDB;Trusted_Connection=true;TrustServerCertificate=true;";
            var context = new SalesDbContext();
            var unitOfWork = new UnitOfWork(context);
            var userRepository = new UserRepository(context);
            _userService = new UserService(unitOfWork, userRepository);
        }

        private void InitializeComponent()
        {
            this.txtUsername = new DevExpress.XtraEditors.TextEdit();
            this.txtPassword = new DevExpress.XtraEditors.TextEdit();
            this.btnLogin = new DevExpress.XtraEditors.SimpleButton();
            this.btnCancel = new DevExpress.XtraEditors.SimpleButton();
            this.lblTitle = new DevExpress.XtraEditors.LabelControl();
            this.lblUsername = new DevExpress.XtraEditors.LabelControl();
            this.lblPassword = new DevExpress.XtraEditors.LabelControl();
            this.pictureEdit1 = new DevExpress.XtraEditors.PictureEdit();

            ((System.ComponentModel.ISupportInitialize)(this.txtUsername.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtPassword.Properties)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureEdit1.Properties)).BeginInit();
            this.SuspendLayout();

            // pictureEdit1
            this.pictureEdit1.Location = new System.Drawing.Point(175, 20);
            this.pictureEdit1.Name = "pictureEdit1";
            this.pictureEdit1.Size = new System.Drawing.Size(100, 80);
            this.pictureEdit1.TabIndex = 0;

            // lblTitle
            this.lblTitle.Appearance.Font = new System.Drawing.Font("Segoe UI", 16F, System.Drawing.FontStyle.Bold);
            this.lblTitle.Appearance.ForeColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.lblTitle.Location = new System.Drawing.Point(120, 110);
            this.lblTitle.Name = "lblTitle";
            this.lblTitle.Size = new System.Drawing.Size(210, 30);
            this.lblTitle.TabIndex = 1;
            this.lblTitle.Text = "نظام إدارة المبيعات";

            // lblUsername
            this.lblUsername.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F);
            this.lblUsername.Location = new System.Drawing.Point(50, 160);
            this.lblUsername.Name = "lblUsername";
            this.lblUsername.Size = new System.Drawing.Size(88, 19);
            this.lblUsername.TabIndex = 2;
            this.lblUsername.Text = "اسم المستخدم:";

            // txtUsername
            this.txtUsername.Location = new System.Drawing.Point(50, 185);
            this.txtUsername.Name = "txtUsername";
            this.txtUsername.Properties.Appearance.Font = new System.Drawing.Font("Segoe UI", 12F);
            this.txtUsername.Properties.Appearance.Options.UseFont = true;
            this.txtUsername.Size = new System.Drawing.Size(350, 30);
            this.txtUsername.TabIndex = 3;

            // lblPassword
            this.lblPassword.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F);
            this.lblPassword.Location = new System.Drawing.Point(50, 230);
            this.lblPassword.Name = "lblPassword";
            this.lblPassword.Size = new System.Drawing.Size(73, 19);
            this.lblPassword.TabIndex = 4;
            this.lblPassword.Text = "كلمة المرور:";

            // txtPassword
            this.txtPassword.Location = new System.Drawing.Point(50, 255);
            this.txtPassword.Name = "txtPassword";
            this.txtPassword.Properties.Appearance.Font = new System.Drawing.Font("Segoe UI", 12F);
            this.txtPassword.Properties.Appearance.Options.UseFont = true;
            this.txtPassword.Properties.PasswordChar = '*';
            this.txtPassword.Properties.UseSystemPasswordChar = true;
            this.txtPassword.Size = new System.Drawing.Size(350, 30);
            this.txtPassword.TabIndex = 5;

            // btnLogin
            this.btnLogin.Appearance.BackColor = System.Drawing.Color.FromArgb(((int)(((byte)(0)))), ((int)(((byte)(122)))), ((int)(((byte)(204)))));
            this.btnLogin.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F, System.Drawing.FontStyle.Bold);
            this.btnLogin.Appearance.ForeColor = System.Drawing.Color.White;
            this.btnLogin.Appearance.Options.UseBackColor = true;
            this.btnLogin.Appearance.Options.UseFont = true;
            this.btnLogin.Appearance.Options.UseForeColor = true;
            this.btnLogin.Location = new System.Drawing.Point(230, 310);
            this.btnLogin.Name = "btnLogin";
            this.btnLogin.Size = new System.Drawing.Size(170, 35);
            this.btnLogin.TabIndex = 6;
            this.btnLogin.Text = "تسجيل الدخول";
            this.btnLogin.Click += new System.EventHandler(this.btnLogin_Click);

            // btnCancel
            this.btnCancel.Appearance.Font = new System.Drawing.Font("Segoe UI", 10F);
            this.btnCancel.Location = new System.Drawing.Point(50, 310);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(170, 35);
            this.btnCancel.TabIndex = 7;
            this.btnCancel.Text = "إلغاء";
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);

            // LoginForm
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(450, 380);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.btnLogin);
            this.Controls.Add(this.txtPassword);
            this.Controls.Add(this.lblPassword);
            this.Controls.Add(this.txtUsername);
            this.Controls.Add(this.lblUsername);
            this.Controls.Add(this.lblTitle);
            this.Controls.Add(this.pictureEdit1);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            this.IconOptions.ShowIcon = false;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "LoginForm";
            this.RightToLeft = System.Windows.Forms.RightToLeft.Yes;
            this.RightToLeftLayout = true;
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "تسجيل الدخول - نظام إدارة المبيعات";

            ((System.ComponentModel.ISupportInitialize)(this.txtUsername.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.txtPassword.Properties)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pictureEdit1.Properties)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();
        }

        private void SetupForm()
        {
            // Additional setup if needed
            // Form properties are already set in InitializeComponent
        }

        private async void btnLogin_Click(object? sender, EventArgs e)
        {
            try
            {
                // التحقق من البيانات المدخلة - Validate input data
                if (string.IsNullOrWhiteSpace(txtUsername.Text))
                {
                    MessageBox.Show("يرجى إدخال اسم المستخدم", "خطأ", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    txtUsername.Focus();
                    return;
                }

                if (string.IsNullOrWhiteSpace(txtPassword.Text))
                {
                    MessageBox.Show("يرجى إدخال كلمة المرور", "خطأ", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    txtPassword.Focus();
                    return;
                }

                // محاولة تسجيل الدخول - Attempt login
                var user = await AuthenticateUserAsync(txtUsername.Text, txtPassword.Text);

                if (user == null)
                {
                    MessageBox.Show("اسم المستخدم أو كلمة المرور غير صحيحة", "خطأ في تسجيل الدخول",
                        MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    txtPassword.Text = "";
                    txtPassword.Focus();
                    return;
                }

                // فتح لوحة التحكم الرئيسية - Open main dashboard
                this.Hide();
                var dashboardForm = new MainDashboardForm(user);
                var result = dashboardForm.ShowDialog();

                if (result == DialogResult.OK)
                {
                    // المستخدم سجل الخروج - User logged out
                    this.Show();
                    txtPassword.Text = "";
                    txtUsername.Focus();
                }
                else
                {
                    // إغلاق التطبيق - Close application
                    this.DialogResult = DialogResult.Cancel;
                    this.Close();
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"خطأ في تسجيل الدخول:\n{ex.Message}", "خطأ", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void btnCancel_Click(object? sender, EventArgs e)
        {
            this.DialogResult = DialogResult.Cancel;
            this.Close();
        }

        private async Task<User?> AuthenticateUserAsync(string username, string password)
        {
            try
            {
                this.Cursor = Cursors.WaitCursor;
                btnLogin.Enabled = false;

                var user = await _userService.AuthenticateAsync(username, password);
                return user;
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"Authentication error: {ex.Message}");
                return null;
            }
            finally
            {
                this.Cursor = Cursors.Default;
                btnLogin.Enabled = true;
            }
        }
    }
}
