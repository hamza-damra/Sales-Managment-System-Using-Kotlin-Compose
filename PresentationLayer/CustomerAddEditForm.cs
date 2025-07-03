using DXApplication1.Models;
using DXApplication1.BusinessLogicLayer;
using DXApplication1.DataAccessLayer;
using DevExpress.XtraEditors;
using System.ComponentModel;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// نموذج إضافة/تعديل العميل - Customer Add/Edit Form
    /// </summary>
    public partial class CustomerAddEditForm : XtraForm
    {
        private Customer? _customer;
        private ICustomerService _customerService = null!;
        private bool _isEditMode;

        // DevExpress Controls
        private TextEdit txtCustomerName = null!;
        private TextEdit txtCompanyName = null!;
        private TextEdit txtPhone = null!;
        private TextEdit txtMobile = null!;
        private TextEdit txtEmail = null!;
        private MemoEdit txtAddress = null!;
        private TextEdit txtCity = null!;
        private TextEdit txtCountry = null!;
        private TextEdit txtPostalCode = null!;
        private TextEdit txtTaxNumber = null!;
        private SpinEdit spnCreditLimit = null!;
        private SpinEdit spnCurrentBalance = null!;
        private ComboBoxEdit cmbCustomerType = null!;
        private MemoEdit txtNotes = null!;
        private CheckEdit chkIsActive = null!;

        private SimpleButton btnSave = null!;
        private SimpleButton btnCancel = null!;

        // Labels
        private LabelControl lblCustomerName = null!;
        private LabelControl lblCompanyName = null!;
        private LabelControl lblPhone = null!;
        private LabelControl lblMobile = null!;
        private LabelControl lblEmail = null!;
        private LabelControl lblAddress = null!;
        private LabelControl lblCity = null!;
        private LabelControl lblCountry = null!;
        private LabelControl lblPostalCode = null!;
        private LabelControl lblTaxNumber = null!;
        private LabelControl lblCreditLimit = null!;
        private LabelControl lblCurrentBalance = null!;
        private LabelControl lblCustomerType = null!;
        private LabelControl lblNotes = null!;

        public CustomerAddEditForm(Customer? customer = null)
        {
            _customer = customer;
            _isEditMode = customer != null;

            InitializeComponent();
            InitializeServices();
            SetupForm();
            LoadData();
        }

        private void InitializeServices()
        {
            try
            {
                // إنشاء الخدمات - Initialize services
                var context = new SalesDbContext();
                var unitOfWork = new UnitOfWork(context);
                _customerService = new CustomerService(unitOfWork, context);

                // Log for debugging
                System.Diagnostics.Debug.WriteLine("تم تهيئة خدمات العملاء في نموذج التحرير بنجاح");
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine($"خطأ في تهيئة خدمات العملاء في نموذج التحرير: {ex}");
                throw new InvalidOperationException("فشل في تهيئة خدمات العملاء", ex);
            }
        }

        private void InitializeComponent()
        {
            // Initialize all controls
            this.txtCustomerName = new TextEdit();
            this.txtCompanyName = new TextEdit();
            this.txtPhone = new TextEdit();
            this.txtMobile = new TextEdit();
            this.txtEmail = new TextEdit();
            this.txtAddress = new MemoEdit();
            this.txtCity = new TextEdit();
            this.txtCountry = new TextEdit();
            this.txtPostalCode = new TextEdit();
            this.txtTaxNumber = new TextEdit();
            this.spnCreditLimit = new SpinEdit();
            this.spnCurrentBalance = new SpinEdit();
            this.cmbCustomerType = new ComboBoxEdit();
            this.txtNotes = new MemoEdit();
            this.chkIsActive = new CheckEdit();

            this.btnSave = new SimpleButton();
            this.btnCancel = new SimpleButton();

            // Initialize labels
            this.lblCustomerName = new LabelControl();
            this.lblCompanyName = new LabelControl();
            this.lblPhone = new LabelControl();
            this.lblMobile = new LabelControl();
            this.lblEmail = new LabelControl();
            this.lblAddress = new LabelControl();
            this.lblCity = new LabelControl();
            this.lblCountry = new LabelControl();
            this.lblPostalCode = new LabelControl();
            this.lblTaxNumber = new LabelControl();
            this.lblCreditLimit = new LabelControl();
            this.lblCurrentBalance = new LabelControl();
            this.lblCustomerType = new LabelControl();
            this.lblNotes = new LabelControl();

            this.SuspendLayout();

            // Form properties
            this.AutoScaleDimensions = new SizeF(6F, 13F);
            this.AutoScaleMode = AutoScaleMode.Font;
            this.ClientSize = new Size(600, 700);
            this.FormBorderStyle = FormBorderStyle.FixedDialog;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.RightToLeft = RightToLeft.Yes;
            this.RightToLeftLayout = true;
            this.StartPosition = FormStartPosition.CenterParent;
            this.Text = _isEditMode ? "تعديل العميل" : "إضافة عميل جديد";
            this.IconOptions.ShowIcon = false;

            // Layout controls
            int yPos = 20;
            int labelWidth = 120;
            int controlWidth = 200;
            int spacing = 35;

            // Customer Name
            SetupLabelAndControl(lblCustomerName, txtCustomerName, "اسم العميل:", 20, yPos, labelWidth, controlWidth, true);
            yPos += spacing;

            // Company Name
            SetupLabelAndControl(lblCompanyName, txtCompanyName, "اسم الشركة:", 20, yPos, labelWidth, controlWidth);
            yPos += spacing;

            // Phone
            SetupLabelAndControl(lblPhone, txtPhone, "الهاتف:", 20, yPos, labelWidth, controlWidth);
            yPos += spacing;

            // Mobile
            SetupLabelAndControl(lblMobile, txtMobile, "الجوال:", 20, yPos, labelWidth, controlWidth);
            yPos += spacing;

            // Email
            SetupLabelAndControl(lblEmail, txtEmail, "البريد الإلكتروني:", 20, yPos, labelWidth, controlWidth);
            yPos += spacing;

            // Customer Type
            SetupLabelAndControl(lblCustomerType, cmbCustomerType, "نوع العميل:", 20, yPos, labelWidth, controlWidth);
            yPos += spacing;

            // City
            SetupLabelAndControl(lblCity, txtCity, "المدينة:", 20, yPos, labelWidth, controlWidth);
            yPos += spacing;

            // Country
            SetupLabelAndControl(lblCountry, txtCountry, "الدولة:", 20, yPos, labelWidth, controlWidth);
            txtCountry.Text = "السعودية"; // Default value
            yPos += spacing;

            // Postal Code
            SetupLabelAndControl(lblPostalCode, txtPostalCode, "الرمز البريدي:", 20, yPos, labelWidth, controlWidth);
            yPos += spacing;

            // Tax Number
            SetupLabelAndControl(lblTaxNumber, txtTaxNumber, "الرقم الضريبي:", 20, yPos, labelWidth, controlWidth);
            yPos += spacing;

            // Credit Limit
            SetupLabelAndControl(lblCreditLimit, spnCreditLimit, "حد الائتمان:", 20, yPos, labelWidth, controlWidth);
            spnCreditLimit.Properties.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnCreditLimit.Properties.DisplayFormat.FormatString = "N2";
            spnCreditLimit.Properties.EditFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
            spnCreditLimit.Properties.EditFormat.FormatString = "N2";
            spnCreditLimit.Properties.MaxValue = 999999999;
            spnCreditLimit.Properties.MinValue = 0;
            yPos += spacing;

            // Current Balance (only in edit mode)
            if (_isEditMode)
            {
                SetupLabelAndControl(lblCurrentBalance, spnCurrentBalance, "الرصيد الحالي:", 20, yPos, labelWidth, controlWidth);
                spnCurrentBalance.Properties.DisplayFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
                spnCurrentBalance.Properties.DisplayFormat.FormatString = "N2";
                spnCurrentBalance.Properties.EditFormat.FormatType = DevExpress.Utils.FormatType.Numeric;
                spnCurrentBalance.Properties.EditFormat.FormatString = "N2";
                spnCurrentBalance.Properties.MaxValue = 999999999;
                spnCurrentBalance.Properties.MinValue = -999999999;
                spnCurrentBalance.Properties.ReadOnly = true; // Balance should be managed through transactions
                yPos += spacing;
            }

            // Address
            lblAddress.Text = "العنوان:";
            lblAddress.Location = new Point(20, yPos);
            lblAddress.Size = new Size(labelWidth, 21);
            txtAddress.Location = new Point(150, yPos);
            txtAddress.Size = new Size(controlWidth + 200, 60);
            this.Controls.Add(lblAddress);
            this.Controls.Add(txtAddress);
            yPos += 70;

            // Notes
            lblNotes.Text = "ملاحظات:";
            lblNotes.Location = new Point(20, yPos);
            lblNotes.Size = new Size(labelWidth, 21);
            txtNotes.Location = new Point(150, yPos);
            txtNotes.Size = new Size(controlWidth + 200, 60);
            this.Controls.Add(lblNotes);
            this.Controls.Add(txtNotes);
            yPos += 70;

            // Is Active
            chkIsActive.Text = "نشط";
            chkIsActive.Location = new Point(150, yPos);
            chkIsActive.Size = new Size(100, 21);
            chkIsActive.Checked = true;
            this.Controls.Add(chkIsActive);
            yPos += spacing;

            // Buttons
            btnSave.Text = "حفظ";
            btnSave.Location = new Point(400, yPos + 20);
            btnSave.Size = new Size(100, 35);
            btnSave.Appearance.BackColor = Color.FromArgb(0, 122, 204);
            btnSave.Appearance.ForeColor = Color.White;
            btnSave.Appearance.Font = new Font("Segoe UI", 10F, FontStyle.Bold);
            btnSave.Appearance.Options.UseBackColor = true;
            btnSave.Appearance.Options.UseForeColor = true;
            btnSave.Appearance.Options.UseFont = true;
            btnSave.Click += btnSave_Click;

            btnCancel.Text = "إلغاء";
            btnCancel.Location = new Point(280, yPos + 20);
            btnCancel.Size = new Size(100, 35);
            btnCancel.Appearance.Font = new Font("Segoe UI", 10F);
            btnCancel.Appearance.Options.UseFont = true;
            btnCancel.Click += btnCancel_Click;

            this.Controls.Add(btnSave);
            this.Controls.Add(btnCancel);

            // Adjust form height
            this.ClientSize = new Size(600, yPos + 80);

            this.ResumeLayout(false);
            this.PerformLayout();
        }

        private void SetupLabelAndControl(LabelControl label, Control control, string labelText, int x, int y, int labelWidth, int controlWidth, bool required = false)
        {
            label.Text = labelText + (required ? " *" : "");
            label.Location = new Point(x, y + 3);
            label.Size = new Size(labelWidth, 21);
            if (required)
            {
                label.Appearance.ForeColor = Color.Red;
                label.Appearance.Options.UseForeColor = true;
            }

            control.Location = new Point(x + labelWidth + 10, y);
            control.Size = new Size(controlWidth, 24);

            this.Controls.Add(label);
            this.Controls.Add(control);
        }

        private void SetupForm()
        {
            // إعداد النموذج - Setup form
            this.Text = _isEditMode ? "تعديل العميل - Edit Customer" : "إضافة عميل جديد - Add New Customer";

            // RTL support
            this.RightToLeft = RightToLeft.Yes;
            this.RightToLeftLayout = true;

            // Set Arabic font
            this.Font = new Font("Segoe UI", 9F, FontStyle.Regular);

            // Setup customer type combo box
            cmbCustomerType.Properties.Items.Clear();
            cmbCustomerType.Properties.Items.Add("فرد");
            cmbCustomerType.Properties.Items.Add("شركة");
            cmbCustomerType.SelectedIndex = 0;

            // Set default values
            chkIsActive.Checked = true;
            spnCreditLimit.Value = 0;
            spnCurrentBalance.Value = 0;

            // Focus on customer name
            txtCustomerName.Focus();
        }

        private void LoadData()
        {
            if (_customer != null)
            {
                txtCustomerName.Text = _customer.CustomerName;
                txtCompanyName.Text = _customer.CompanyName;
                txtPhone.Text = _customer.Phone;
                txtMobile.Text = _customer.Mobile;
                txtEmail.Text = _customer.Email;
                txtAddress.Text = _customer.Address;
                txtCity.Text = _customer.City;
                txtCountry.Text = _customer.Country;
                txtPostalCode.Text = _customer.PostalCode;
                txtTaxNumber.Text = _customer.TaxNumber;
                spnCreditLimit.Value = _customer.CreditLimit;
                if (_isEditMode)
                    spnCurrentBalance.Value = _customer.CurrentBalance;
                txtNotes.Text = _customer.Notes;
                chkIsActive.Checked = _customer.IsActive;

                // Set customer type
                cmbCustomerType.SelectedIndex = _customer.CustomerType == CustomerType.Individual ? 0 : 1;
            }
        }

        private async void btnSave_Click(object? sender, EventArgs e)
        {
            try
            {
                if (!ValidateForm())
                    return;

                this.Cursor = Cursors.WaitCursor;
                btnSave.Enabled = false;

                var customer = CreateCustomerFromForm();
                bool success;

                if (_isEditMode)
                {
                    customer.Id = _customer!.Id;
                    success = await _customerService.UpdateCustomerAsync(customer);
                }
                else
                {
                    success = await _customerService.CreateCustomerAsync(customer);
                }

                if (success)
                {
                    XtraMessageBox.Show(
                        _isEditMode ? "تم تحديث العميل بنجاح" : "تم إضافة العميل بنجاح",
                        "نجح", MessageBoxButtons.OK, MessageBoxIcon.Information);

                    this.DialogResult = DialogResult.OK;
                    this.Close();
                }
                else
                {
                    XtraMessageBox.Show(
                        _isEditMode ? "فشل في تحديث العميل" : "فشل في إضافة العميل",
                        "خطأ", MessageBoxButtons.OK, MessageBoxIcon.Error);
                }
            }
            catch (ArgumentException ex)
            {
                XtraMessageBox.Show($"خطأ في البيانات المدخلة:\n{ex.Message}", "خطأ في التحقق من البيانات",
                    MessageBoxButtons.OK, MessageBoxIcon.Warning);
            }
            catch (InvalidOperationException ex)
            {
                XtraMessageBox.Show($"خطأ في العملية:\n{ex.Message}", "خطأ في العملية",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            catch (Exception ex)
            {
                XtraMessageBox.Show($"خطأ غير متوقع في حفظ البيانات:\n{ex.Message}\n\nيرجى المحاولة مرة أخرى أو الاتصال بالدعم الفني.", "خطأ",
                    MessageBoxButtons.OK, MessageBoxIcon.Error);

                // Log the full exception for debugging
                System.Diagnostics.Debug.WriteLine($"خطأ في حفظ العميل: {ex}");
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

        private bool ValidateForm()
        {
            var errors = new List<string>();

            // التحقق من اسم العميل - Validate customer name
            if (string.IsNullOrWhiteSpace(txtCustomerName.Text))
                errors.Add("• اسم العميل مطلوب");
            else if (txtCustomerName.Text.Trim().Length < 2)
                errors.Add("• اسم العميل يجب أن يكون أكثر من حرفين");

            // التحقق من البريد الإلكتروني - Validate email
            if (!string.IsNullOrWhiteSpace(txtEmail.Text) && !IsValidEmail(txtEmail.Text))
                errors.Add("• البريد الإلكتروني غير صحيح");

            // التحقق من أرقام الهاتف - Validate phone numbers
            if (!string.IsNullOrWhiteSpace(txtPhone.Text) && txtPhone.Text.Trim().Length < 7)
                errors.Add("• رقم الهاتف غير صحيح");

            if (!string.IsNullOrWhiteSpace(txtMobile.Text) && txtMobile.Text.Trim().Length < 10)
                errors.Add("• رقم الجوال غير صحيح");

            // التحقق من الحدود المالية - Validate financial limits
            if (spnCreditLimit.Value < 0)
                errors.Add("• حد الائتمان لا يمكن أن يكون سالباً");

            if (_isEditMode && spnCurrentBalance.Value < 0)
                errors.Add("• الرصيد الحالي لا يمكن أن يكون سالباً");

            // التحقق من نوع العميل والشركة - Validate customer type and company
            if (cmbCustomerType.SelectedIndex == 1 && string.IsNullOrWhiteSpace(txtCompanyName.Text))
                errors.Add("• اسم الشركة مطلوب عند اختيار نوع العميل 'شركة'");

            if (errors.Count > 0)
            {
                XtraMessageBox.Show($"يرجى تصحيح الأخطاء التالية:\n\n{string.Join("\n", errors)}",
                    "خطأ في البيانات المدخلة", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return false;
            }

            return true;
        }

        private Customer CreateCustomerFromForm()
        {
            var customerType = cmbCustomerType.SelectedIndex == 0 ? CustomerType.Individual : CustomerType.Company;

            return new Customer
            {
                CustomerName = txtCustomerName.Text.Trim(),
                CompanyName = string.IsNullOrWhiteSpace(txtCompanyName.Text) ? null : txtCompanyName.Text.Trim(),
                Phone = string.IsNullOrWhiteSpace(txtPhone.Text) ? null : txtPhone.Text.Trim(),
                Mobile = string.IsNullOrWhiteSpace(txtMobile.Text) ? null : txtMobile.Text.Trim(),
                Email = string.IsNullOrWhiteSpace(txtEmail.Text) ? null : txtEmail.Text.Trim(),
                Address = string.IsNullOrWhiteSpace(txtAddress.Text) ? null : txtAddress.Text.Trim(),
                City = string.IsNullOrWhiteSpace(txtCity.Text) ? null : txtCity.Text.Trim(),
                Country = string.IsNullOrWhiteSpace(txtCountry.Text) ? null : txtCountry.Text.Trim(),
                PostalCode = string.IsNullOrWhiteSpace(txtPostalCode.Text) ? null : txtPostalCode.Text.Trim(),
                TaxNumber = string.IsNullOrWhiteSpace(txtTaxNumber.Text) ? null : txtTaxNumber.Text.Trim(),
                CreditLimit = spnCreditLimit.Value,
                CurrentBalance = _isEditMode ? spnCurrentBalance.Value : 0,
                CustomerType = customerType,
                Notes = string.IsNullOrWhiteSpace(txtNotes.Text) ? null : txtNotes.Text.Trim(),
                IsActive = chkIsActive.Checked
            };
        }

        private static bool IsValidEmail(string email)
        {
            try
            {
                var addr = new System.Net.Mail.MailAddress(email);
                return addr.Address == email;
            }
            catch
            {
                return false;
            }
        }
    }
}
