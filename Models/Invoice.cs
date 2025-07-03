using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace DXApplication1.Models
{
    /// <summary>
    /// كيان الفاتورة - Invoice Entity
    /// </summary>
    public class Invoice : BaseEntity
    {
        [Required]
        [MaxLength(50)]
        public string InvoiceNumber { get; set; } = string.Empty;

        [Required]
        public DateTime InvoiceDate { get; set; } = DateTime.Now;

        public DateTime? DueDate { get; set; }

        [Required]
        public int CustomerId { get; set; }
        public virtual Customer Customer { get; set; } = null!;

        [Required]
        public int UserId { get; set; }
        public virtual User User { get; set; } = null!;

        [Column(TypeName = "decimal(18,2)")]
        public decimal SubTotal { get; set; } = 0;

        [Column(TypeName = "decimal(18,2)")]
        public decimal TaxAmount { get; set; } = 0;

        [Column(TypeName = "decimal(18,2)")]
        public decimal DiscountAmount { get; set; } = 0;

        [Column(TypeName = "decimal(5,2)")]
        public decimal DiscountPercentage { get; set; } = 0;

        [Column(TypeName = "decimal(18,2)")]
        public decimal TotalAmount { get; set; } = 0;

        [Column(TypeName = "decimal(18,2)")]
        public decimal PaidAmount { get; set; } = 0;

        [Column(TypeName = "decimal(18,2)")]
        public decimal RemainingAmount { get; set; } = 0;

        public InvoiceStatus Status { get; set; } = InvoiceStatus.Draft;

        public PaymentMethod PaymentMethod { get; set; } = PaymentMethod.Cash;

        [MaxLength(1000)]
        public string? Notes { get; set; }

        [MaxLength(500)]
        public string? Terms { get; set; }

        public bool IsPrinted { get; set; } = false;

        public DateTime? PrintedDate { get; set; }

        // Navigation properties
        public virtual ICollection<InvoiceItem> InvoiceItems { get; set; } = new List<InvoiceItem>();
        public virtual ICollection<Payment> Payments { get; set; } = new List<Payment>();
    }

    /// <summary>
    /// حالة الفاتورة - Invoice Status
    /// </summary>
    public enum InvoiceStatus
    {
        Draft = 1,      // مسودة
        Pending = 2,    // معلقة
        Paid = 3,       // مدفوعة
        Cancelled = 4,  // ملغية
        Overdue = 5     // متأخرة
    }

    /// <summary>
    /// طريقة الدفع - Payment Method
    /// </summary>
    public enum PaymentMethod
    {
        Cash = 1,           // نقدي
        CreditCard = 2,     // بطاقة ائتمان
        BankTransfer = 3,   // تحويل بنكي
        Check = 4,          // شيك
        Other = 5           // أخرى
    }
}
