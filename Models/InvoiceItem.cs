using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace DXApplication1.Models
{
    /// <summary>
    /// كيان عنصر الفاتورة - Invoice Item Entity
    /// </summary>
    public class InvoiceItem : BaseEntity
    {
        [Required]
        public int InvoiceId { get; set; }
        public virtual Invoice Invoice { get; set; } = null!;

        [Required]
        public int ProductId { get; set; }
        public virtual Product Product { get; set; } = null!;

        [Required]
        [Column(TypeName = "decimal(18,3)")]
        public decimal Quantity { get; set; } = 1;

        [Required]
        [Column(TypeName = "decimal(18,2)")]
        public decimal UnitPrice { get; set; } = 0;

        [Column(TypeName = "decimal(18,2)")]
        public decimal DiscountAmount { get; set; } = 0;

        [Column(TypeName = "decimal(5,2)")]
        public decimal DiscountPercentage { get; set; } = 0;

        [Column(TypeName = "decimal(5,2)")]
        public decimal TaxRate { get; set; } = 0;

        [Column(TypeName = "decimal(18,2)")]
        public decimal TaxAmount { get; set; } = 0;

        [Column(TypeName = "decimal(18,2)")]
        public decimal LineTotal { get; set; } = 0;

        [MaxLength(500)]
        public string? Notes { get; set; }

        public int LineNumber { get; set; } = 1;

        // Calculated properties
        [NotMapped]
        public decimal SubTotal => Quantity * UnitPrice;

        [NotMapped]
        public decimal NetAmount => SubTotal - DiscountAmount;
    }
}
