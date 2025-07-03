using System.ComponentModel.DataAnnotations;

namespace DXApplication1.Models
{
    /// <summary>
    /// كيان العميل - Customer Entity
    /// </summary>
    public class Customer : BaseEntity
    {
        [Required]
        [MaxLength(200)]
        public string CustomerName { get; set; } = string.Empty;

        [MaxLength(100)]
        public string? CompanyName { get; set; }

        [MaxLength(20)]
        public string? Phone { get; set; }

        [MaxLength(20)]
        public string? Mobile { get; set; }

        [MaxLength(100)]
        public string? Email { get; set; }

        [MaxLength(500)]
        public string? Address { get; set; }

        [MaxLength(100)]
        public string? City { get; set; }

        [MaxLength(100)]
        public string? Country { get; set; }

        [MaxLength(20)]
        public string? PostalCode { get; set; }

        [MaxLength(50)]
        public string? TaxNumber { get; set; }

        public decimal CreditLimit { get; set; } = 0;

        public decimal CurrentBalance { get; set; } = 0;

        public CustomerType CustomerType { get; set; } = CustomerType.Individual;

        [MaxLength(1000)]
        public string? Notes { get; set; }

        // Navigation properties
        public virtual ICollection<Invoice> Invoices { get; set; } = new List<Invoice>();
    }

    /// <summary>
    /// نوع العميل - Customer Type
    /// </summary>
    public enum CustomerType
    {
        Individual = 1,  // فرد
        Company = 2      // شركة
    }
}
