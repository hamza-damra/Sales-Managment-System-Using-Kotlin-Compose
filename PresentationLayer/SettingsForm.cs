using DevExpress.XtraEditors;
using System.Drawing;
using System.Windows.Forms;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// نموذج الإعدادات - Settings Form
    /// </summary>
    public partial class SettingsForm : XtraForm
    {
        public SettingsForm()
        {
            InitializeComponent();
            SetupForm();
        }

        private void InitializeComponent()
        {
            var lblMessage = new LabelControl();
            var btnClose = new SimpleButton();

            this.SuspendLayout();

            // lblMessage
            lblMessage.Appearance.Font = new Font("Segoe UI", 12F);
            lblMessage.Location = new Point(200, 150);
            lblMessage.Name = "lblMessage";
            lblMessage.Size = new Size(300, 21);
            lblMessage.TabIndex = 0;
            lblMessage.Text = "نموذج الإعدادات قيد التطوير";

            // btnClose
            btnClose.Appearance.Font = new Font("Segoe UI", 10F);
            btnClose.Location = new Point(250, 200);
            btnClose.Name = "btnClose";
            btnClose.Size = new Size(100, 30);
            btnClose.TabIndex = 1;
            btnClose.Text = "إغلاق";
            btnClose.Click += (s, e) => this.Close();

            // SettingsForm
            this.AutoScaleDimensions = new SizeF(6F, 13F);
            this.AutoScaleMode = AutoScaleMode.Font;
            this.ClientSize = new Size(600, 400);
            this.Controls.Add(btnClose);
            this.Controls.Add(lblMessage);
            this.IconOptions.ShowIcon = false;
            this.Name = "SettingsForm";
            this.RightToLeft = RightToLeft.Yes;
            this.RightToLeftLayout = true;
            this.StartPosition = FormStartPosition.CenterParent;
            this.Text = "الإعدادات";

            this.ResumeLayout(false);
            this.PerformLayout();
        }

        private void SetupForm()
        {
            // Additional setup if needed
        }
    }
}
