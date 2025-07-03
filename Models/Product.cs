using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace DXApplication1.Models
{
    /// <summary>
    /// كيان المنتج - Product Entity
    /// </summary>
    public class Product : BaseEntity
    {
        [Required]
        [MaxLength(200)]
        public string ProductName { get; set; } = string.Empty;

        [MaxLength(100)]
        public string? ProductCode { get; set; }

        [MaxLength(100)]
        public string? Barcode { get; set; }

        [MaxLength(500)]
        public string? Description { get; set; }

        [Required]
        public int CategoryId { get; set; }
        public virtual Category Category { get; set; } = null!;

        [Column(TypeName = "decimal(18,2)")]
        public decimal PurchasePrice { get; set; } = 0;

        [Column(TypeName = "decimal(18,2)")]
        public decimal SalePrice { get; set; } = 0;

        [Column(TypeName = "decimal(18,2)")]
        public decimal MinimumPrice { get; set; } = 0;

        public int StockQuantity { get; set; } = 0;

        public int MinimumStock { get; set; } = 0;

        public int MaximumStock { get; set; } = 0;

        [MaxLength(50)]
        public string? Unit { get; set; } = "قطعة";

        [Column(TypeName = "decimal(5,2)")]
        public decimal TaxRate { get; set; } = 0;

        public bool TrackInventory { get; set; } = true;

        public bool AllowNegativeStock { get; set; } = false;

        [MaxLength(500)]
        public string? ImagePath { get; set; }

        [MaxLength(1000)]
        public string? Notes { get; set; }

        // Navigation properties
        public virtual ICollection<InvoiceItem> InvoiceItems { get; set; } = new List<InvoiceItem>();
    }
}
