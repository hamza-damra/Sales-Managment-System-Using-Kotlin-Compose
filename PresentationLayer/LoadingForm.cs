using DevExpress.XtraEditors;
using DevExpress.XtraWaitForm;
using System;
using System.Drawing;
using System.Windows.Forms;

namespace DXApplication1.PresentationLayer
{
    /// <summary>
    /// نموذج التحميل - Loading Form
    /// </summary>
    public partial class LoadingForm : WaitForm
    {
        public LoadingForm()
        {
            InitializeComponent();
        }

        private void InitializeComponent()
        {
            var progressPanel = new ProgressPanel();
            var lblMessage = new LabelControl();

            this.SuspendLayout();

            // progressPanel
            progressPanel.Appearance.BackColor = Color.Transparent;
            progressPanel.Appearance.Options.UseBackColor = true;
            progressPanel.AppearanceCaption.Font = new Font("Segoe UI", 10F);
            progressPanel.AppearanceCaption.Options.UseFont = true;
            progressPanel.AppearanceDescription.Font = new Font("Segoe UI", 8.25F);
            progressPanel.AppearanceDescription.Options.UseFont = true;
            progressPanel.Caption = "جاري التحميل...";
            progressPanel.Description = "يرجى الانتظار";
            progressPanel.Dock = DockStyle.Fill;
            progressPanel.ImageHorzOffset = 20;
            progressPanel.Name = "progressPanel";
            progressPanel.Size = new Size(246, 73);
            progressPanel.TabIndex = 0;
            progressPanel.Text = "progressPanel";

            // LoadingForm
            this.AutoScaleDimensions = new SizeF(6F, 13F);
            this.AutoScaleMode = AutoScaleMode.Font;
            this.ClientSize = new Size(246, 73);
            this.Controls.Add(progressPanel);
            this.DoubleBuffered = true;
            this.Name = "LoadingForm";
            this.StartPosition = FormStartPosition.CenterScreen;

            this.Text = "تحميل";
            this.FormBorderStyle = FormBorderStyle.None;
            this.ShowInTaskbar = false;
            this.RightToLeft = RightToLeft.Yes;
            this.RightToLeftLayout = true;

            this.ResumeLayout(false);
        }

        public void SetMessage(string message)
        {
            if (this.Controls.Count > 0 && this.Controls[0] is ProgressPanel panel)
            {
                panel.Caption = message;
            }
        }

        public void SetProgressDescription(string description)
        {
            if (this.Controls.Count > 0 && this.Controls[0] is ProgressPanel panel)
            {
                panel.Description = description;
            }
        }

        #region Overrides

        public override void SetCaption(string caption)
        {
            SetMessage(caption);
        }

        public override void SetDescription(string description)
        {
            SetProgressDescription(description);
        }

        public override void ProcessCommand(Enum cmd, object arg)
        {
            // Handle commands if needed
        }

        #endregion
    }
}
