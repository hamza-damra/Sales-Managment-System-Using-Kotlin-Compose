using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace DXApplication1.Models
{
    /// <summary>
    /// كيان الدفعة - Payment Entity
    /// </summary>
    public class Payment : BaseEntity
    {
        [Required]
        public int InvoiceId { get; set; }
        public virtual Invoice Invoice { get; set; } = null!;

        [Required]
        [Column(TypeName = "decimal(18,2)")]
        public decimal Amount { get; set; } = 0;

        [Required]
        public DateTime PaymentDate { get; set; } = DateTime.Now;

        public PaymentMethod PaymentMethod { get; set; } = PaymentMethod.Cash;

        [MaxLength(100)]
        public string? ReferenceNumber { get; set; }

        [MaxLength(500)]
        public string? Notes { get; set; }

        [Required]
        public int UserId { get; set; }
        public virtual User User { get; set; } = null!;
    }
}
